use std::{
    env, fs,
    path::PathBuf,
    process::{exit, Command},
};

use log::info;
use tauri::Manager;
use tauri_plugin_log::{
    fern::colors::{Color, ColoredLevelConfig},
    Target, TargetKind,
};

use crate::{settings::get_settings, setup::setup_app};

mod actions;
mod apps;
mod plugins;
mod search;
mod settings;
mod setup;
mod state;
mod utils;

pub fn run() {
    // TODO: Make Setting for Compositing
    std::env::set_var("WEBKIT_DISABLE_COMPOSITING_MODE", "1");
    // std::env::set_var("GDK_BACKEND", "x11");

    if env::args().any(|arg| arg == "--killserver" || arg == "-k") {
        info!("Killing Mordomo by request");

        let path = PathBuf::from("/tmp/mordomo/main.sock");

        if path.exists() {
            Command::new("pkill")
                .arg("mordomo")
                .spawn()
                .expect("Failed to kill mordomo process");

            fs::remove_file(&path).expect("Failed to remove socket");
        }

        exit(0);
    }

    tauri::Builder::default()
        .plugin(tauri_plugin_opener::init())
        .plugin(tauri_plugin_dialog::init())
        .plugin(
            tauri_plugin_log::Builder::new()
                .targets([Target::new(TargetKind::Stdout)])
                .level(log::LevelFilter::Debug)
                .with_colors(ColoredLevelConfig {
                    error: Color::Red,
                    warn: Color::Yellow,
                    debug: Color::Blue,
                    info: Color::BrightGreen,
                    trace: Color::Cyan,
                })
                .build(),
        )
        .invoke_handler(tauri::generate_handler![get_settings])
        .setup(|app| {
            let app_clone = app.app_handle().to_owned();

            tokio::task::spawn(async {
                setup_app(app_clone).await.expect("Error initiating app");
            });

            Ok(())
        })
        .on_window_event(|window, event| match event {
            tauri::WindowEvent::CloseRequested { api, .. } => {
                if window.label() == "mordomo" {
                    api.prevent_close();
                    window.hide().expect("Failed to hide mordomo window");
                }
            }
            _ => {}
        })
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
