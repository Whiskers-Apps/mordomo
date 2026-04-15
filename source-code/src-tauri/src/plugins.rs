use std::{
    error::Error,
    fs::{self},
    path::PathBuf,
    process::Command,
    thread,
};

use log::{debug, info};
use mordomo_core::{
    core::{Entry, FormSubmittedMessage, PluginMessage},
    settings,
};
use serde::{Deserialize, Serialize};
use tauri::{AppHandle, Emitter, Listener};
use tokio::{
    io::{AsyncReadExt, AsyncWriteExt},
    net::TcpListener,
    sync::broadcast,
};

use crate::utils::get_state;

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct PluginInfo {
    pub id: String,
    pub name: String,
    pub description: String,

    #[serde(default)]
    pub settings: Option<Vec<PluginSetting>>,

    #[serde(default)]
    pub dir: Option<PathBuf>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
#[serde(rename_all = "snake_case")]
pub enum PluginSetting {
    Text(TextSetting),
    Number(NumberSetting),
    Select(SelectSetting),
    Check(CheckSetting),
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct TextSetting {
    pub id: String,
    pub default_value: String,
    pub title: String,
    pub description: Option<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct NumberSetting {
    pub id: String,
    pub default_value: usize,
    pub min: usize,
    pub max: usize,
    pub title: String,
    pub description: Option<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct SelectSetting {
    pub id: String,
    pub default_value: String,
    pub options: Vec<SelectOption>,
    pub title: String,
    pub description: Option<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct SelectOption {
    pub id: String,
    pub text: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct CheckSetting {
    pub id: String,
    pub default_value: bool,
    pub title: String,
    pub description: Option<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct SendFormToPluginPayload {
    pub message: FormSubmittedMessage,
}

// -------------------------------------------------------------------------- //

pub async fn setup_plugins_socket(app: AppHandle) -> Result<(), Box<dyn Error>> {
    let listener = TcpListener::bind("127.0.0.1:0").await?;
    let port = listener.local_addr()?.port();

    println!("Plugins Socket Port: [{}]", &port);

    let (transimitter, _) = broadcast::channel::<Vec<u8>>(16);
    let transmiter_for_event = transimitter.clone();
    let transmiter_for_form_event = transimitter.clone();

    app.clone().listen("send-to-plugin", move |event| {
        if let Ok(message) = serde_json::from_str::<PluginMessage>(event.payload()) {
            // println!("Sending Message: {:?}", &message);
            let bytes = postcard::to_allocvec(&message).unwrap();
            let _ = transmiter_for_event.send(bytes);
        }
    });

    app.clone().listen("send-form-to-plugin", move |event| {
        let payload = serde_json::from_str::<SendFormToPluginPayload>(event.payload())
            .expect("Error getting payload");

        let plugin_message = PluginMessage::FormSubmitted(payload.message);

        let bytes = postcard::to_allocvec(&plugin_message).unwrap();
        let _ = transmiter_for_form_event.send(bytes);
    });

    let app_for_listener = app.clone();
    let app_for_plugins = app.clone();

    tokio::spawn(async move {
        while let Ok((stream, _)) = listener.accept().await {
            let (mut reader, mut writer) = tokio::io::split(stream);

            let mut receiver = transimitter.subscribe();

            let app_for_search = app_for_listener.clone();

            tokio::spawn(async move {
                let mut buffer = [0u8; 1024 * 300];

                while let Ok(n) = reader.read(&mut buffer).await {
                    if n == 0 {
                        break;
                    }

                    if let Ok(entries) = postcard::from_bytes::<Vec<Entry>>(&buffer[..n]) {
                        app_for_search.emit("set-entries", entries).unwrap();
                    }
                }
            });

            tokio::spawn(async move {
                while let Ok(bytes) = receiver.recv().await {
                    if writer.write_all(&bytes).await.is_err() {
                        break;
                    }
                }
            });
        }
    });

    let plugins = get_plugins()?;

    let mut state = get_state(&app_for_plugins);
    state.plugins = plugins.clone();

    // Add Plugin Settings If They Don't Exist
    let mut plugins_settings = state.settings.plugins.clone();

    for info in plugins.clone() {
        if let Some(info_settings) = info.settings.clone() {
            for info_setting in info_settings.clone() {
                let info_setting_id = match info_setting.clone() {
                    PluginSetting::Text(text_setting) => text_setting.id,
                    PluginSetting::Number(number_setting) => number_setting.id,
                    PluginSetting::Select(select_setting) => select_setting.id,
                    PluginSetting::Check(check_setting) => check_setting.id,
                };

                let app_plugin_setting = plugins_settings
                    .clone()
                    .into_iter()
                    .find(|ps| ps.plugin_id == info.id && ps.setting_id == info_setting_id);

                if app_plugin_setting.is_none() {
                    plugins_settings.push(settings::PluginSetting {
                        plugin_id: info.id.clone(),
                        setting_id: info_setting_id.clone(),
                        value: match info_setting.clone() {
                            PluginSetting::Text(text_setting) => text_setting.default_value,
                            PluginSetting::Number(number_setting) => {
                                number_setting.default_value.to_string()
                            }
                            PluginSetting::Select(select_setting) => {
                                select_setting.default_value.to_string()
                            }
                            PluginSetting::Check(check_setting) => {
                                check_setting.default_value.to_string()
                            }
                        },
                    });
                }
            }
        }
    }

    state.settings.plugins = plugins_settings;

    state.settings.save()?;

    // Execute Plugins
    for plugin in plugins.clone() {
        thread::spawn(move || {
            info!("Executing {} plugin", &plugin.id);

            Command::new("./extension")
                .arg(port.to_string())
                .current_dir(&plugin.dir.unwrap())
                .spawn()
                .expect(format!("Failed to execute {} extension", &plugin.id).as_str());
        });
    }

    Ok(())
}

fn get_plugins() -> Result<Vec<PluginInfo>, Box<dyn Error>> {
    let mut dir = dirs::data_local_dir().ok_or_else(|| "Failed to get local dir")?;
    dir.push("mordomo/plugins");

    if !dir.exists() {
        fs::create_dir_all(&dir)?;
    }

    let plugins_dirs: Vec<PathBuf> = fs::read_dir(&dir)?
        .filter_map(|entry| entry.ok())
        .filter(|entry| entry.path().is_dir())
        .map(|entry| entry.path())
        .collect();

    let plugins: Vec<PluginInfo> = plugins_dirs
        .iter()
        .filter_map(|dir| {
            let info_path = dir.join("info.json");
            let bytes = fs::read(&info_path).ok()?;
            let mut info = serde_json::from_slice::<PluginInfo>(&bytes).ok()?;

            info.dir = Some(dir.to_owned());

            Some(info)
        })
        .collect();

    Ok(plugins)
}
