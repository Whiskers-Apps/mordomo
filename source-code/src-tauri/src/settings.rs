use std::{error::Error, fs, sync::Mutex};

use dirs::cache_dir;
use mordomo_core::settings::Settings;
use tauri::{AppHandle, Manager};

use crate::state::AppState;

// -------------------------------------------------------- //

/// Reads user settings and saves them in app state
pub fn setup_settings(app: AppHandle) -> Result<(), Box<dyn Error>> {
    let state = app.state::<Mutex<AppState>>();
    let mut state = state.lock().expect("");

    let mut settings_path =
        dirs::config_dir().ok_or_else(|| String::from("Failed to get config dir"))?;

    settings_path.push("mordomo");

    if !settings_path.exists() {
        fs::create_dir_all(&settings_path)?;
    }

    settings_path.push("settings.json");

    if !settings_path.exists() {
        let settings = Settings::default();
        let json = serde_json::to_string_pretty(&settings)?;

        fs::write(&settings_path, &json)?;

        state.settings = settings;

        return Ok(());
    }

    let json = fs::read(&settings_path)?;
    let user_settings = serde_json::from_slice(&json)?;

    state.settings = user_settings;

    let search_engine_icons_path = cache_dir()
        .ok_or_else(|| "Failed to get cache dir")?
        .join("mordomo")
        .join("search-engine-icons");

    if !search_engine_icons_path.exists() {
        fs::create_dir_all(&search_engine_icons_path)?;
    }

    for search_engine in &state.settings.search_engines {
        let engine_icon_path = search_engine_icons_path
            .clone()
            .join(&search_engine.id.to_string());

        let engine_query = search_engine.query.to_owned();

        tokio::spawn(async move {
            let mut website = engine_query
                .replace("https://", "")
                .replace("http://", "")
                .replace("%s", "");

            if website.ends_with("/") {
                website = website.strip_suffix("/").unwrap().to_string();
            }

            let url = format!("https://favicon.vemetric.com/{website}&size=128");

            let response = reqwest::get(&url).await.expect("Error making reqwest");

            if !response.status().is_success() {
                return;
            }

            let bytes = response.bytes().await.expect("Error getting response");

            fs::write(&engine_icon_path, &bytes).expect("Error creating favicon file");
        });
    }

    Ok(())
}

#[tauri::command]
pub fn get_settings(app: AppHandle) -> Result<Settings, String> {
    let state = app.state::<Mutex<AppState>>();
    let state = state.lock().map_err(|e| e.to_string())?;

    Ok(state.settings.to_owned())
}
