use tauri::Manager;

use crate::setup::setup_app;

mod apps;
mod search;
mod settings;
mod setup;
mod state;

pub fn run() {
    tracing_subscriber::fmt::init();
    // TODO: Make This Optional but disabled by default

    std::env::set_var("WEBKIT_DISABLE_COMPOSITING_MODE", "1");
    // std::env::set_var("GDK_BACKEND", "x11");

    tauri::Builder::default()
        .plugin(tauri_plugin_opener::init())
        .invoke_handler(tauri::generate_handler![])
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
