use std::{
    error::Error,
    fs,
    io::{Read, Write},
    os::unix::net::{UnixListener, UnixStream},
    path::PathBuf,
    process::{exit, Command},
    sync::Mutex,
    thread,
};

use mordomo_plugin::{IpcMessage, MainMessage, PluginMessage};
use serde::{Deserialize, Serialize};
use tauri::{AppHandle, Emitter, Listener, Manager, WebviewUrl, WebviewWindowBuilder};
use tokio::{
    io::{AsyncReadExt, AsyncWriteExt},
    net::TcpListener,
    sync::broadcast,
};
use tracing::{error, info, warn};

use crate::{apps::setup_apps, search::setup_search, state::AppState};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Plugin {
    pub dir: PathBuf,
    pub id: String,
    pub name: String,
    pub description: String,
}

pub async fn setup_app(app: AppHandle) -> Result<(), Box<dyn Error>> {
    let window_app = app.clone();
    let main_socket_app = app.clone();
    let plugins_socket_app = app.clone();
    let search_app = app.clone();
    let apps_app = app.clone();

    app.manage(Mutex::new(AppState::default()));

    setup_main_socket(main_socket_app)?;
    setup_plugins_socket(plugins_socket_app).await?;
    setup_window(window_app)?;
    setup_search(search_app)?;
    setup_apps(apps_app).await?;

    Ok(())
}

fn setup_main_socket(app: AppHandle) -> Result<(), Box<dyn Error>> {
    let tmp_dir = PathBuf::from("/tmp/mordomo/");
    let socket_path = PathBuf::from("/tmp/mordomo/main.sock");

    if socket_path.exists() {
        match UnixStream::connect(&socket_path) {
            Ok(mut stream) => {
                info!("Socket already exists. Showing main window");

                let message = MainMessage::Show;
                let bytes = postcard::to_allocvec(&message)?;
                stream.write_all(&bytes)?;

                exit(0);
            }
            Err(_) => {
                warn!("Failed to connect main socket");

                fs::remove_file(&socket_path)?;

                setup_main_socket(app.clone()).expect("Failed to connect socket");
            }
        }
    }

    if !tmp_dir.exists() {
        fs::create_dir_all(&tmp_dir)?;
    }

    thread::spawn(move || {
        if let Ok(listener) = UnixListener::bind(&socket_path) {
            for stream in listener.incoming() {
                match stream {
                    Ok(mut stream) => {
                        let mut buffer = vec![0u8; 1024];

                        let bytes_size = stream
                            .read(&mut buffer)
                            .expect("Error reading main socket stream");

                        let message: MainMessage = postcard::from_bytes(&buffer[..bytes_size])
                            .expect("Error converting bytes to message");

                        match message {
                            MainMessage::Show => {
                                let window = app
                                    .get_webview_window("mordomo")
                                    .expect("Failed to get main window");

                                window.show().expect("Failed to show main window");
                            }
                            _ => {}
                        }
                    }
                    Err(_) => {
                        error!("Failed to get stream from socket");
                    }
                }
            }
        } else {
            fs::remove_file(&socket_path).expect("Error removing socket");

            info!("Removing old socket");

            setup_main_socket(app.clone()).expect("Failed to connect socket");
        }
    });

    Ok(())
}

async fn setup_plugins_socket(app: AppHandle) -> Result<(), Box<dyn Error>> {
    // let home_dir = dirs::home_dir().expect("Failed to get home directory");
    // let plugins_dir = home_dir.clone().join(".local/share/mordomo/plugins");

    let listener = TcpListener::bind("127.0.0.1:6969").await?;
    let (transimitter, _) = broadcast::channel::<Vec<u8>>(16);
    let transmiter_for_event = transimitter.clone();

    app.listen("send-to-plugin", move |event| {
        if let Ok(message) = serde_json::from_str::<IpcMessage>(event.payload()) {
            let bytes = postcard::to_allocvec(&message).unwrap();
            let _ = transmiter_for_event.send(bytes);
        }
    });

    tokio::spawn(async move {
        while let Ok((stream, _)) = listener.accept().await {
            let (mut reader, mut writer) = tokio::io::split(stream);

            let mut receiver = transimitter.subscribe();

            let app_for_search = app.clone();

            tokio::spawn(async move {
                let mut buffer = [0u8; 1024];
                while let Ok(n) = reader.read(&mut buffer).await {
                    if n == 0 {
                        break;
                    }
                    if let Ok(message) = postcard::from_bytes::<IpcMessage>(&buffer[..n]) {
                        match message {
                            IpcMessage::Main(main_message) => match main_message {
                                MainMessage::Entries(entries) => {
                                    app_for_search.emit("set-entries", entries).unwrap();
                                }
                                _ => {}
                            },
                            _ => {}
                        }
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

    let plugins = vec![
        Plugin {
            dir: PathBuf::from("/home/lighttigerxiv/.local/share/mordomo/plugins/core-session/"),
            id: "core-session".to_string(),
            name: "Session".to_string(),
            description: "bla bla bla".to_string(),
        },
        Plugin {
            dir: PathBuf::from("/home/lighttigerxiv/.local/share/mordomo/plugins/core-bookmarks/"),
            id: "core-bookmarks".to_string(),
            name: "Bookmarks".to_string(),
            description: "bla bla bla".to_string(),
        },
    ];

    for plugin in plugins {
        thread::spawn(move || {
            Command::new("./extension")
                .current_dir(&plugin.dir)
                .spawn()
                .expect(format!("Failed to execute {} extension", &plugin.id).as_str());
        });
    }

    Ok(())
}

fn setup_window(app: AppHandle) -> Result<(), Box<dyn Error>> {
    let window = app
        .get_webview_window("main")
        .expect("Failed to get window");

    window.close().expect("Failed to close main window");

    // TODO: Obter tamanho pelas settings

    WebviewWindowBuilder::new(&app, "mordomo", WebviewUrl::App("index.html".into()))
        .title("mordomo")
        .center()
        .always_on_top(true)
        .decorations(false)
        .inner_size(600.0, 400.0)
        .resizable(false)
        .build()
        .expect("Failed to build window");

    Ok(())
}
