use mordomo_core::settings::Settings;
use serde::{Deserialize, Serialize};

use crate::{apps::App, plugins::PluginInfo};

#[derive(Serialize, Deserialize)]
#[serde(default)]
pub struct AppState {
    pub settings: Settings,
    pub apps: Vec<App>,
    pub plugins: Vec<PluginInfo>,
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
