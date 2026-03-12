use std::error::Error;

use dirs::cache_dir;
use mordomo_core::{
    core::{Action, Entry, GetEntriesMessage, OpenApp, OpenURL, PluginMessage},
    settings::{Keyword, SearchEngine},
    utils::KeywordSplit,
};
use serde::{Deserialize, Serialize};
use sniffer_rs::sniffer::Sniffer;
use tauri::{AppHandle, Emitter, Listener};

use crate::{apps::App, plugins::PluginInfo, utils::get_state};

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

            if text.trim().is_empty() {
                app_for_emitter
                    .emit("set-entries", Vec::<Entry>::new())
                    .expect("Failed to send empty entries");

                return;
            }

            let state = get_state(&app);

            if keyword_split.has_keyword {
                check_plugins(
                    &keyword_split,
                    &state.plugins,
                    &state.settings.keywords,
                    &app,
                )
                .expect("Failed to send plugin message");

                check_search_engines(&keyword_split, &state.settings.search_engines, &app)
                    .expect("Failed to send search engine entry");

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

fn check_search_engines(
    keyword_split: &KeywordSplit,
    search_engines: &[SearchEngine],
    app: &AppHandle,
) -> Result<(), Box<dyn Error>> {
    let keyword = keyword_split.keyword.to_owned();

    let search_engine = match search_engines.iter().find(|se| se.keyword == keyword) {
        Some(se) => se,
        None => return Ok(()),
    };

    let url = search_engine
        .query
        .clone()
        .replace("%s", &keyword_split.text);

    let icon_path = cache_dir()
        .ok_or_else(|| "Failed to get cache dir")?
        .join("mordomo")
        .join("search-engine-icons")
        .join(&search_engine.id.to_string());

    let mut entry = Entry::new(&search_engine.name)
        .set_subtext(format!("Search {}", &keyword_split.text))
        .set_action(Action::OpenURL(OpenURL::new(url)));

    if icon_path.exists() {
        entry.set_icon_path(&icon_path);
    }

    app.emit("set-entries", vec![entry])?;

    Ok(())
}
