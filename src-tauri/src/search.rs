use std::{error::Error, sync::Mutex};

use mordomo_plugin::{
    core::{Action, Entry, GetEntriesMessage, OpenApp, PluginMessage},
    utils::KeywordSplit,
};
use serde::{Deserialize, Serialize};
use sniffer_rs::sniffer::Sniffer;
use tauri::{AppHandle, Emitter, Listener, Manager};

use crate::{apps::App, plugins::PluginInfo, settings::Keyword, state::AppState, utils::get_state};

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
            let keyword_split = KeywordSplit::from(&text);

            let state = get_state(&app);

            if keyword_split.has_keyword {
                check_plugins(
                    &keyword_split,
                    &state.plugins,
                    &state.settings.keywords,
                    &app,
                )
                .expect("Failed to send plugin message");

                return;
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

fn check_plugins(
    keyword_split: &KeywordSplit,
    plugins: &[PluginInfo],
    keywords: &[Keyword],
    app: &AppHandle,
) -> Result<(), Box<dyn Error>> {
    let keyword = keyword_split.keyword.to_owned();

    let plugin_id = match keywords.iter().find(|k| k.keyword == keyword) {
        Some(k) => k.plugin_id.to_owned(),
        None => return Ok(()),
    };

    let plugin_info = match plugins.iter().find(|info| info.id == plugin_id) {
        Some(info) => info,
        None => return Ok(()),
    };

    app.emit(
        "send-to-plugin",
        PluginMessage::GetEntries(GetEntriesMessage {
            plugin_id: plugin_info.id.to_owned(),
            search_text: keyword_split.text.to_owned(),
        }),
    )?;

    Ok(())
}

fn get_app_entry(app: &App) -> Entry {
    let action = Action::OpenApp(OpenApp::new(app.path.to_owned()));
    let mut entry = Entry::new(app.name.to_owned()).set_action(action);

    if let Some(description) = app.description.to_owned() {
        entry.set_subtext(description);
    } else {
        entry.set_subtext("Application");
    };

    if let Some(icon_path) = app.icon_path.to_owned() {
        entry.set_icon_path(icon_path);
    }

    entry
}
