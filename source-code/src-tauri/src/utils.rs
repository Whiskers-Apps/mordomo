use std::{
    error::Error,
    sync::{Mutex, MutexGuard},
};

use tauri::{AppHandle, Manager, WebviewWindow};

use crate::state::AppState;

pub fn get_window(app: &AppHandle) -> Result<WebviewWindow, Box<dyn Error>> {
    let window = app
        .get_webview_window("mordomo")
        .ok_or_else(|| "Failed to get window".to_string())?;

    Ok(window)
}

pub fn get_state(app: &AppHandle) -> MutexGuard<'_, AppState> {
    app.state::<Mutex<AppState>>()
        .inner()
        .lock()
        .expect("Error")
}
