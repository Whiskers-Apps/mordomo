use std::{
    error::Error,
    process::{Command, Stdio},
    thread,
};

use clap::error::Result;
use mordomo_core::core::{
    Action, CopyImage, CopyText, OpenApp, OpenFile, OpenURL, PluginMessage, RunCustomActionMessage,
    RunOnPlugin, ShowEntries,
};
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
            Action::OpenFile(action) => {
                open_file(action, &app_for_event).expect("Failed to open file/directory");
            }
            Action::OpenURL(action) => {
                open_url(action, &app_for_event).expect("Failed to open url");
            }
            Action::CopyText(action) => {
                copy_text(action, &app_for_event).expect("Failed to copy text");
            }
            Action::CopyImage(action) => {
                copy_image(action, &app_for_event).expect("Failed to copy image");
            }
            Action::ShowEntries(action) => {
                show_entries(action, &app_for_event).expect("Failed to show entries");
            }
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

fn open_file(action: OpenFile, app: &AppHandle) -> Result<(), Box<dyn Error>> {
    open::that_detached(&action.path)?;
    get_window(app)?.close()?;
    Ok(())
}

fn show_entries(action: ShowEntries, app: &AppHandle) -> Result<(), Box<dyn Error>> {
    app.emit("set-entries", action.entries)?;

    Ok(())
}

fn run_on_plugin(action: RunOnPlugin, app: &AppHandle) -> Result<(), Box<dyn Error>> {
    let payload = PluginMessage::RunCustomAction(RunCustomActionMessage {
        plugin_id: action.plugin_id,
        action: action.action,
        custom_info: action.custom_info,
    });

    println!("Payload sent: {:?}", &payload);

    app.emit("send-to-plugin", payload)?;

    get_window(&app)?.close()?;

    Ok(())
}

fn open_url(action: OpenURL, app: &AppHandle) -> Result<(), Box<dyn Error>> {
    open::that_detached(action.url)?;

    get_window(app)?.close()?;

    Ok(())
}

fn copy_text(action: CopyText, app: &AppHandle) -> Result<(), Box<dyn Error>> {
    tokio::spawn(async move {
        let text_blocks: Vec<&str> = action.text.split(" ").collect();

        Command::new("wl-copy")
            .args(text_blocks)
            .spawn()
            .expect("Error copying to clipboard");
    });

    get_window(app)?.close()?;

    Ok(())
}

fn copy_image(action: CopyImage, app: &AppHandle) -> Result<(), Box<dyn Error>> {
    tokio::spawn(async move {
        Command::new("cat")
            .arg(format!("'{}'", action.image_path.display()))
            .args(["|", "wl-copy", "-t", "image/png"])
            .stdout(Stdio::piped())
            .stdin(Stdio::piped())
            .spawn()
            .expect("Error copying image")
    });

    get_window(app)?.close()?;

    Ok(())
}
