use std::{
    error::Error,
    fs,
    path::PathBuf,
    sync::{mpsc::channel, Mutex},
    thread,
};

use freedesktop_desktop_entry::{default_paths, get_languages_from_env, DesktopEntry, Iter};
use notify::{Event, Watcher};
use serde::{Deserialize, Serialize};
use tauri::{AppHandle, Manager};
use tracing::info;
use tux_icons::icon_fetcher::IconFetcher;

use crate::state::AppState;

#[derive(Serialize, Deserialize, Debug, Clone)]
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

    let cache_dir = dirs::cache_dir().ok_or_else(|| "Failed to get cache dir".to_string())?;
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
    let entries = Iter::new(default_paths())
        .entries(Some(&locales))
        .collect::<Vec<_>>()
        .into_iter()
        .filter_map(|entry| {
            if entry.no_display() {
                return None;
            }

            if let Some(type_) = entry.type_() {
                if type_ == "Application" {
                    return Some(entry);
                }
            }

            None
        })
        .collect::<Vec<DesktopEntry>>();

    let mut apps: Vec<App> = vec![];

    for entry in entries {
        let name = match entry.name(&locales) {
            Some(name) => name.to_string(),
            None => continue,
        };

        let description = match entry.comment(&locales) {
            Some(description) => Some(description.to_string()),
            None => None,
        };

        let keywords: Vec<String> = match entry.keywords(&locales) {
            Some(keywords) => keywords.into_iter().map(|key| key.to_string()).collect(),
            None => vec![],
        };

        let icon_path = if let Some(icon) = entry.icon() {
            icon_fetcher.get_icon_path(icon)
        } else {
            None
        };

        apps.push(App {
            name,
            description,
            keywords,
            path: entry.path,
            icon_path: icon_path,
        });
    }

    let cache_dir = dirs::cache_dir().ok_or_else(|| "Failed to get cache dir".to_string())?;
    let apps_path = cache_dir.clone().join("mordomo/apps.bin");

    let bytes = postcard::to_allocvec(&apps)?;
    fs::write(&apps_path, &bytes)?;

    let state = app.state::<Mutex<AppState>>();

    let mut state = state.lock().unwrap();
    state.apps = apps;

    Ok(())
}
