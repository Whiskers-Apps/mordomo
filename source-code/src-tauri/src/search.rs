use std::error::Error;

use mordomo_core::{
    core::{Action, Entry, GetEntriesMessage, OpenApp, OpenURL, PluginMessage},
    settings::{Keyword, SearchEngine},
    utils::KeywordSplit,
};
use regex::Regex;
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
            let mut entries = Vec::<Entry>::new();

            if text.trim().is_empty() {
                app_for_emitter
                    .emit("set-entries", Vec::<Entry>::new())
                    .expect("Failed to send empty entries");

                return;
            }

            let state = get_state(&app);

            if keyword_split.has_keyword {
                let found_plugin = check_plugins(
                    &keyword_split,
                    &state.plugins,
                    &state.settings.keywords,
                    &app,
                )
                .expect("Failed to send plugin message");

                let found_engine =
                    check_search_engines(&keyword_split, &state.settings.search_engines, &app)
                        .expect("Failed to send search engine entry");

                if found_plugin || found_engine {
                    return;
                }
            }

            let url_regex =
                Regex::new(r"https?://(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,}(?:/[^\s]*)?").unwrap();

            let https_appended = format!("https://{}", &text);

            if url_regex.is_match(&https_appended) {
                entries.push(
                    Entry::new("Website")
                        .set_subtext(format!("Go to {}", &https_appended))
                        .set_action(Action::OpenURL(OpenURL::new(https_appended)))
                        .add_custom_info("{icon}-globe"),
                );

                app_for_emitter
                    .emit("set-entries", entries)
                    .expect("Failed to emit");

                return;
            }

            let apps = state.apps.to_owned();
            let sniffer = Sniffer::new();

            entries = apps
                .iter()
                .filter_map(|app| match sniffer.matches(&app.name, &text) {
                    true => Some(get_app_entry(app)),
                    false => None,
                })
                .collect::<Vec<Entry>>();

            if entries.is_empty() {
                let default_engine_id = state.settings.default_engine.clone();
                let search_engines = state.settings.search_engines.clone();

                if let Some(engine_id) = default_engine_id {
                    if let Some(search_engine) = search_engines.iter().find(|se| se.id == engine_id)
                    {
                        let mut entry = Entry::new(&search_engine.name)
                            .set_subtext(format!("Search {}", &text))
                            .set_action(Action::OpenURL(OpenURL::new(
                                &search_engine.query.replace("%s", &text),
                            )));

                        if let Ok(icon_path) = search_engine.get_icon() {
                            if icon_path.exists() {
                                entry.set_icon_path(&icon_path);
                            }
                        }

                        entries.push(entry);
                    }
                }
            }

            app_for_emitter
                .emit("set-entries", entries)
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
) -> Result<bool, Box<dyn Error>> {
    let keyword = keyword_split.keyword.to_owned();

    let plugin_id = match keywords.iter().find(|k| k.keyword == keyword) {
        Some(k) => k.plugin_id.to_owned(),
        None => return Ok(false),
    };

    let plugin_info = match plugins.iter().find(|info| info.id == plugin_id) {
        Some(info) => info,
        None => return Ok(false),
    };

    app.emit(
        "send-to-plugin",
        PluginMessage::GetEntries(GetEntriesMessage {
            plugin_id: plugin_info.id.to_owned(),
            search_text: keyword_split.text.to_owned(),
        }),
    )?;

    Ok(true)
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
) -> Result<bool, Box<dyn Error>> {
    let keyword = keyword_split.keyword.to_owned();

    let search_engine = match search_engines.iter().find(|se| se.keyword == keyword) {
        Some(se) => se,
        None => return Ok(false),
    };

    let url = search_engine
        .query
        .clone()
        .replace("%s", &keyword_split.text);

    let mut entry = Entry::new(&search_engine.name)
        .set_subtext(format!("Search {}", &keyword_split.text))
        .set_action(Action::OpenURL(OpenURL::new(url)));

    if let Ok(icon_path) = search_engine.get_icon() {
        if icon_path.exists() {
            entry.set_icon_path(&icon_path);
        }
    }

    app.emit("set-entries", vec![entry])?;

    Ok(true)
}
