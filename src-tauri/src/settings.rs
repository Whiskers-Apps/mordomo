use std::{error::Error, fs, sync::Mutex};

use serde::{Deserialize, Serialize};
use tauri::{AppHandle, Manager};

use crate::state::AppState;

#[derive(Serialize, Deserialize, Debug, Clone)]
#[serde(default)]
pub struct Settings {
    pub width: u16,
    pub height: u16,
    pub theme: Theme,
    pub keywords: Vec<Keyword>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
#[serde(default)]
pub struct Theme {
    pub main: String,
    pub secondary: String,
    pub tertiary: String,
    pub text_main: String,
    pub text_secondary: String,
    pub text_disabled: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Keyword {
    pub plugin_id: String,
    pub keyword: String,
}

impl Default for Settings {
    fn default() -> Self {
        Self {
            width: 600,
            height: 400,
            theme: Theme::default(),
            keywords: vec![],
        }
    }
}

impl Default for Theme {
    fn default() -> Self {
        Self {
            main: String::from("#141414"),
            secondary: String::from("#1F1F1F"),
            tertiary: String::from("#383838"),
            text_main: String::from("#F2F2F2"),
            text_secondary: String::from("#E5E5E5"),
            text_disabled: String::from("#9F9F9F"),
        }
    }
}

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

    Ok(())
}

#[tauri::command]
pub fn get_settings(app: AppHandle) -> Result<Settings, String> {
    let state = app.state::<Mutex<AppState>>();
    let state = state.lock().map_err(|e| e.to_string())?;

    Ok(state.settings.to_owned())
}
