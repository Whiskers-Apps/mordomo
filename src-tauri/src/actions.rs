use std::{error::Error, process::Command, thread};

use mordomo_plugin::core::{Action, OpenApp, PluginMessage, RunCustomActionMessage, RunOnPlugin};
use serde::{Deserialize, Serialize};
use tauri::{AppHandle, Emitter, Listener};

use crate::utils::get_window;

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ExecActionPayload {
    pub action: Action,
}

pub fn setup_actions(app: AppHandle) {
    let app_for_event = app.clone();

    app.listen("exec-action", move |event| {
        let payload = serde_json::from_str::<ExecActionPayload>(event.payload())
            .expect("Error getting payload");

        match payload.action {
            Action::OpenApp(action) => {
                open_app(action, &app_for_event).expect("Failed to open app");
            }
            Action::OpenFile(_open_file) => {}
            Action::OpenURL(_open_url) => {}
            Action::CopyText(_copy_text) => {}
            Action::CopyImage(_copy_image) => {}
            Action::ShowEntries(_show_entries) => {}
            Action::RunOnPlugin(action) => {
                run_on_plugin(action, &app_for_event).expect("Failed to run plugin action");
            }
            Action::Core => {}
        }
    });
}

fn open_app(action: OpenApp, app: &AppHandle) -> Result<(), Box<dyn Error>> {
    let path = action.path.to_owned();

    let file_name = path
        .file_name()
        .ok_or("Error getting desktop file name".to_string())?
        .to_owned();

    thread::spawn(move || {
        Command::new("gtk-launch")
            .arg(&file_name)
            .spawn()
            .expect("Failed to open app using gtk-launch");
    });

    get_window(&app)?.close()?;

    Ok(())
}

fn run_on_plugin(action: RunOnPlugin, app: &AppHandle) -> Result<(), Box<dyn Error>> {
    let payload = PluginMessage::RunCustomAction(RunCustomActionMessage {
        plugin_id: action.plugin_id,
        action: action.action,
        custom_info: action.custom_info,
    });

    app.emit("send-to-plugin", payload)?;

    get_window(&app)?.close()?;

    Ok(())
}
