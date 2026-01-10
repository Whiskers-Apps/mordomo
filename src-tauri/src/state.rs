use serde::{Deserialize, Serialize};

use crate::{apps::App, settings::Settings, setup::Plugin};

#[derive(Serialize, Deserialize)]
#[serde(default)]
pub struct AppState {
    pub settings: Settings,
    pub apps: Vec<App>,
    pub plugins: Vec<Plugin>,
}

impl Default for AppState {
    fn default() -> Self {
        Self {
            settings: Settings::default(),
            apps: vec![],
            plugins: vec![],
        }
    }
}
