use std::{
    error::Error,
    fs,
    path::PathBuf,
    sync::{mpsc::channel, Mutex},
    thread,
};

use freedesktop_desktop_entry::{default_paths, get_languages_from_env, DesktopEntry, Iter};
use log::info;
use notify::{Event, Watcher};
use serde::{Deserialize, Serialize};
use tauri::{AppHandle, Manager};
use tux_icons::icon_fetcher::IconFetcher;

use crate::state::AppState;

#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)]
pub struct App {
    pub name: String,
    pub description: Option<String>,
    pub keywords: Vec<String>,
    pub path: PathBuf,
    pub icon_path: Option<PathBuf>,
}

pub async fn setup_apps(app: AppHandle) -> Result<(), Box<dyn Error>> {
    let app_for_indexing = app.clone();
    let app_for_events = app.clone();

    let cache_dir = dirs::cache_dir().ok_or("Failed to get cache directory")?;
    let apps_path = cache_dir.clone().join("mordomo/apps.bin");

    if apps_path.exists() {
        let bytes = fs::read(&apps_path)?;
        let apps = postcard::from_bytes::<Vec<App>>(&bytes)?;

        let state = app.state::<Mutex<AppState>>();

        let mut state = state.lock().unwrap();
        state.apps = apps;
    };

    tokio::spawn(async move { index_apps(app_for_indexing).expect("Failed to index apps") });

    thread::spawn(move || {
        let (notify_event_transimiter, notify_event_receiver) = channel::<notify::Result<Event>>();
        let mut watcher =
            notify::recommended_watcher(notify_event_transimiter).expect("Failed to get watcher");

        for path in default_paths() {
            if !path.exists() {
                continue;
            }

            let _ = watcher.watch(&path, notify::RecursiveMode::Recursive);
        }

        for notify_result in notify_event_receiver {
            match notify_result {
                Ok(event) => match event.kind {
                    notify::EventKind::Create(_) => {
                        let _ = index_apps(app_for_events.clone());
                    }
                    notify::EventKind::Modify(modify_kind) => match modify_kind {
                        notify::event::ModifyKind::Data(_data_change) => {
                            let _ = index_apps(app_for_events.clone());
                        }
                        _ => {}
                    },
                    notify::EventKind::Remove(_) => {
                        let _ = index_apps(app_for_events.clone());
                    }
                    _ => {}
                },
                Err(_) => {}
            }
        }
    });

    Ok(())
}

fn index_apps(app: AppHandle) -> Result<(), Box<dyn Error>> {
    info!("Indexing Apps");

    let icon_fetcher = IconFetcher::new().set_return_target_path(true);

    let locales = get_languages_from_env();

    // Removes Possible Repeated Entries like .local/share/applications
    let mut distinct_paths: Vec<PathBuf> = Vec::new();
    let mut distinct_entries: Vec<DesktopEntry> = Vec::new();
    let all_entries: Vec<DesktopEntry> =
        Iter::new(default_paths()).entries(Some(&locales)).collect();

    for entry in &all_entries {
        if !distinct_paths.contains(&entry.path) {
            distinct_paths.push(entry.path.to_owned());
            distinct_entries.push(entry.to_owned());
        }
    }

    let apps: Vec<App> = distinct_entries
        .iter()
        .filter_map(|entry| {
            if entry.no_display() || entry.type_() != Some("Application") {
                return None;
            }

            let name = entry.name(&locales)?.to_string();

            let description = match entry.comment(&locales) {
                Some(description) => Some(description.to_string()),
                None => None,
            };

            let keywords: Vec<String> = entry
                .keywords(&locales)
                .unwrap_or(vec![])
                .iter()
                .map(|key| key.to_string())
                .collect();

            let icon_path = match entry.icon() {
                Some(icon) => icon_fetcher.get_icon_path(icon),
                None => None,
            };

            Some(App {
                name,
                description,
                keywords: keywords,
                path: entry.path.to_owned(),
                icon_path,
            })
        })
        .collect();

    let cache_dir = dirs::cache_dir().ok_or("Failed to get cache directory")?;
    let apps_path = cache_dir.join("mordomo/apps.bin");

    let bytes = postcard::to_allocvec(&apps)?;
    fs::write(&apps_path, &bytes)?;

    let state = app.state::<Mutex<AppState>>();

    let mut state = state.lock().unwrap();
    state.apps = apps;

    info!("Finished Indexing Apps");

    Ok(())
}
