use std::{
    error::Error,
    fs::{self},
    path::PathBuf,
    process::Command,
    sync::Mutex,
    thread,
};

use mordomo_plugin::core::{Entry, PluginMessage};
use serde::{Deserialize, Serialize};
use tauri::{AppHandle, Emitter, Listener, Manager};
use tokio::{
    io::{AsyncReadExt, AsyncWriteExt},
    net::TcpListener,
    sync::broadcast,
};

use crate::{state::AppState, utils::get_state};

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct PluginInfo {
    pub id: String,
    pub name: String,
    pub description: String,

    #[serde(default)]
    pub dir: Option<PathBuf>,
}

pub async fn setup_plugins_socket(app: AppHandle) -> Result<(), Box<dyn Error>> {
    let listener = TcpListener::bind("127.0.0.1:0").await?;
    let port = listener.local_addr()?.port();

    let (transimitter, _) = broadcast::channel::<Vec<u8>>(16);
    let transmiter_for_event = transimitter.clone();

    app.clone().listen("send-to-plugin", move |event| {
        if let Ok(message) = serde_json::from_str::<PluginMessage>(event.payload()) {
            let bytes = postcard::to_allocvec(&message).unwrap();
            let _ = transmiter_for_event.send(bytes);
        }
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

    for plugin in get_plugins()? {
        thread::spawn(move || {
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
