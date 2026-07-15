use std::{error::Error, process::Command, thread};

use log::info;
use mordomo_core::{
    core::{Entry, FormSubmittedMessage, PluginMessage},
    plugins::get_plugins,
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
pub struct SendFormToPluginPayload {
    pub message: FormSubmittedMessage,
}

// -------------------------------------------------------------------------- //

pub async fn setup_plugins_socket(app: AppHandle) -> Result<(), Box<dyn Error>> {
    let listener = TcpListener::bind("127.0.0.1:0").await?;
    let port = listener.local_addr()?.port();

    info!("Plugins Socket Port: [{}]", &port);

    let (transimitter, _) = broadcast::channel::<Vec<u8>>(16);
    let transmiter_for_event = transimitter.clone();
    let transmiter_for_form_event = transimitter.clone();

    app.clone().listen("send-to-plugin", move |event| {
        if let Ok(message) = serde_json::from_str::<PluginMessage>(event.payload()) {
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

    // Execute Plugins
    for plugin in plugins.clone() {
        thread::spawn(move || {
            info!("Executing {} plugin", &plugin.id);

            Command::new("./plugin")
                .arg(port.to_string())
                .current_dir(&plugin.dir.unwrap())
                .spawn()
                .expect(format!("Failed to execute {} extension", &plugin.id).as_str());
        });
    }

    Ok(())
}
