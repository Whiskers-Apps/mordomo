import { invoke } from "@tauri-apps/api/core";
import type { Settings } from "./Settings"

export class SettingsRepository {
	settings: Settings | null = null;
	settingsCallback?: (settings: Settings) => void;

	constructor() {
		this.getSettings();
	}

	private async getSettings() {
		this.settings = await invoke<Settings>("get_settings");

		if (this.settingsCallback) {
			this.settingsCallback(this.settings);
		}
	}

	getHTML(settings: Settings): string {
		return `
<style>
:root{
	--main: ${settings.theme.main};
	--secondary: ${settings.theme.secondary};
	--tertiary: ${settings.theme.tertiary};
	--text_main: ${settings.theme.text_main};
	--text_secondary: ${settings.theme.text_secondary};
	--text_disabled: ${settings.theme.text_disabled};
	--accent: ${settings.theme.accent};
	--on_accent: ${settings.theme.on_accent};
}
</style>
`
	}
}
