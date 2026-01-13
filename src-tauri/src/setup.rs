use std::{
    error::Error,
    fs,
    io::{Read, Write},
    os::unix::net::{UnixListener, UnixStream},
    path::PathBuf,
    process::exit,
    sync::Mutex,
    thread,
};

use mordomo_plugin::core::MainMessage;
use serde::{Deserialize, Serialize};
use tauri::{AppHandle, Manager, WebviewUrl, WebviewWindowBuilder};
use tracing::{error, info, warn};

use crate::{
    actions::setup_actions, apps::setup_apps, plugins::setup_plugins_socket, search::setup_search,
    settings::setup_settings, state::AppState,
};

// #[derive(Debug, Clone, Serialize, Deserialize)]
// pub struct Plugin {
//     pub dir: PathBuf,
//     pub id: String,
//     pub name: String,
//     pub description: String,
// }

pub async fn setup_app(app: AppHandle) -> Result<(), Box<dyn Error>> {
    // Set Default State
    app.manage(Mutex::new(AppState::default()));

    setup_settings(app.clone())?;
    setup_main_socket(app.clone())?;
    setup_plugins_socket(app.clone()).await?;
    setup_window(app.clone())?;
    setup_search(app.clone())?;
    setup_apps(app.clone()).await?;
    setup_actions(app.clone());

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

fn setup_window(app: AppHandle) -> Result<(), Box<dyn Error>> {
    let window = app
        .get_webview_window("main")
        .expect("Failed to get window");

    window.close().expect("Failed to close main window");

    let state = app.state::<Mutex<AppState>>();
    let state = state.lock().map_err(|e| e.to_string())?;

    WebviewWindowBuilder::new(&app, "mordomo", WebviewUrl::App("index.html".into()))
        .title("mordomo")
        .center()
        .always_on_top(true)
        .decorations(false)
        .inner_size(state.settings.width as f64, state.settings.height as f64)
        .resizable(false)
        .build()?
        .hide()?;

    Ok(())
}
