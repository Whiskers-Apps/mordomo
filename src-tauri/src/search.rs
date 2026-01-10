use std::{error::Error, sync::Mutex};

use mordomo_plugin::{Entry, GetEntriesMessage, IpcMessage, PluginMessage};
use serde::{Deserialize, Serialize};
use sniffer_rs::sniffer::Sniffer;
use tauri::{AppHandle, Emitter, Listener, Manager};

use crate::{apps::App, state::AppState};

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct OnSearchPayload {
    pub text: String,
}

pub fn setup_search(app: AppHandle) -> Result<(), Box<dyn Error>> {
    let app_for_listener = app.clone();
    let app_for_emitter = app.clone();

    app_for_listener.listen("on-search", move |event| {
        if let Ok(search_payload) = serde_json::from_str::<OnSearchPayload>(&event.payload()) {
            let text = search_payload.text;

            let state = app.state::<Mutex<AppState>>();
            let state = state.lock().expect("Failed to get app state");

            if text.starts_with("sm ") {
                app.emit(
                    "send-to-plugin",
                    IpcMessage::Plugin(PluginMessage::GetEntries(GetEntriesMessage {
                        plugin_id: "core-session".to_string(),
                        search_text: text.to_owned(),
                    })),
                )
                .expect("Failed to send message to plugin");
            }

            if text.starts_with("bm ") {
                app.emit(
                    "send-to-plugin",
                    IpcMessage::Plugin(PluginMessage::GetEntries(GetEntriesMessage {
                        plugin_id: "core-bookmarks".to_string(),
                        search_text: text.clone(),
                    })),
                )
                .expect("Failed to send message to plugin");
            }

            let apps = state.apps.to_owned();
            let sniffer = Sniffer::new();

            let app_entries = apps
                .iter()
                .filter_map(|app| match sniffer.matches(&app.name, &text) {
                    true => Some(get_app_entry(app)),
                    false => None,
                })
                .collect::<Vec<Entry>>();

            app_for_emitter
                .emit("set-entries", app_entries)
                .expect("Failed to emit");
        }
    });

    Ok(())
}

fn get_app_entry(app: &App) -> Entry {
    return Entry {
        text: app.name.to_string(),
    };
}
