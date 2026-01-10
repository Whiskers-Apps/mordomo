use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Debug, Clone)]
#[serde(default)]
pub struct Settings {
    pub width: u16,
    pub height: u16,
}

impl Default for Settings {
    fn default() -> Self {
        Self {
            width: 600,
            height: 400,
        }
    }
}
