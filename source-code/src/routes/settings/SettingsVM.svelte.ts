import { goto } from "$app/navigation";
import type { Settings } from "$lib/features/settings/Settings";
import { SettingsRepository } from "$lib/features/settings/SettingsRepository.svelte";


export type Intent = { intent: "back" } | { intent: "keydown", event: KeyboardEvent }

export class SettingsVM {
	state = $state<{
		html: string
	}>({
		html: ""
	});

	constructor() {
		this.addSettingsListener()
	}

	private addSettingsListener() {
		const settingsRepo = new SettingsRepository();

		settingsRepo.settingsCallback = (settings: Settings) => {
			this.state.html = settingsRepo.getHTML(settings);
		}
	}

	//-------------------------Intents----------------------------------//

	onIntent(intent: Intent) {
		switch (intent.intent) {
			case "back": {
				this.onBack();
				break;
			}

			case "keydown": {
				this.onKeydown(intent.event);
				break;
			}
		}
	}

	private onBack() {
		goto("/");
	}

	private onKeydown(event: KeyboardEvent) {
		if (event.key === "Escape") {
			goto("/");
		}
	}
}
