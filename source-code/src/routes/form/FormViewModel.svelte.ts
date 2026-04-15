import { emit, once } from "@tauri-apps/api/event";
import type { CheckEntry, Form, NumberEntry, PathEntry, SelectEntry, TextEntry } from "../MainViewModel.svelte";
import { SettingsRepository } from "$lib/features/settings/SettingsRepository.svelte";
import type { Settings } from "$lib/features/settings/Settings";
import { goto } from "$app/navigation";
import { open } from '@tauri-apps/plugin-dialog';

type FormSubmittedMessage = {
	plugin_id: string;
	results: FormResult[];
	custom_info: string[] | null
}

type FormResult = TextFormResult | NumberFormResult | CheckFormResult | PathFormResult | SelectFormResult

type TextFormResult = {
	TextFormResult: {
		id: string;
		value: string;
		custom_info: string[] | null
	}
}


type NumberFormResult = {
	NumberFormResult: {
		id: string;
		value: number;
		custom_info: string[] | null
	}
}


type CheckFormResult = {
	CheckFormResult: {
		id: string;
		value: boolean;
		custom_info: string[] | null
	}
}


type PathFormResult = {
	PathFormResult: {
		id: string;
		value: string | null;
		custom_info: string[] | null
	}
}

type SelectFormResult = {
	SelectFormResult: {
		id: string;
		value: string;
		custom_info: string[] | null
	}
}
export type Intent = { intent: "backClick" }
	| { intent: "text_entry_type", id: string, value: string }
	| { intent: "number_entry_type", id: string, value: number }
	| { intent: "check_entry_check", id: string, value: boolean }
	| { intent: "path_entry_click", entry: PathEntry }
	| { intent: "clear_path_click", id: string }
	| { intent: "select_entry_click", id: string, value: string }
	| { intent: "keydown", event: KeyboardEvent }
	| { intent: "primaryButtonClick" };


export class FormVM {
	state = $state<{
		loading: boolean,
		html: string,
		form: Form,
		primaryButtonEnabled: boolean
	}>({
		loading: true,
		html: "",
		form: {
			plugin_id: "",
			title: "",
			positive_button_text: "",
			entries: [],
			custom_info: null
		},
		primaryButtonEnabled: false
	})

	constructor() {
		const settingsRepo = new SettingsRepository();

		settingsRepo.settingsCallback = (settings: Settings) => {
			this.state.html = settingsRepo.getHTML(settings);
			this.state.loading = this.isLoading();
		}


		once<Form>("get-form", (event) => {
			this.state.form = event.payload;
			this.state.loading = this.isLoading()
		});


		emit("form-ack");
	}

	private isLoading(): boolean {
		return this.state.form.plugin_id === "" || this.state.html === ""
	}

	onIntent(intent: Intent) {
		switch (intent.intent) {
			case "backClick": {
				this.onBackClick();
				break;
			}
			case "text_entry_type": {
				this.onTextEntryType(intent.id, intent.value);
				break;
			}
			case "number_entry_type": {
				this.onNumberEntryType(intent.id, intent.value)
				break;
			}
			case "check_entry_check": {
				this.onCheckEntryCheck(intent.id, intent.value)
				break;
			}
			case "path_entry_click": {
				this.onPathEntryClick(intent.entry)
				break;
			}
			case "clear_path_click": {
				this.onClearPathClick(intent.id)
				break;
			}
			case "select_entry_click": {
				this.onSelectEntryClick(intent.id, intent.value)
				break;
			}
			case "keydown": {
				this.onKeydown(intent.event);
				break;
			}
			case "primaryButtonClick": {
				this.onPrimaryButtonClick();
				break;
			}
		}
	}

	private onBackClick() {
		this.goBack();
	}

	private onTextEntryType(id: string, value: string) {
		let newEntries = this.state.form.entries;

		let index = newEntries.findIndex((entry) => {
			return "TextEntry" in entry && entry.TextEntry.id === id
		});

		(newEntries[index] as TextEntry).TextEntry.value = value

		let newForm = this.state.form;
		newForm.entries = newEntries;

		this.state.form = newForm
	}

	private onNumberEntryType(id: string, value: number) {
		let newEntries = this.state.form.entries;

		let index = newEntries.findIndex((entry) => {
			return "NumberEntry" in entry && entry.NumberEntry.id === id
		});

		(newEntries[index] as NumberEntry).NumberEntry.value = value

		let newForm = this.state.form;
		newForm.entries = newEntries;

		this.state.form = newForm
	}

	private onCheckEntryCheck(id: string, value: boolean) {
		let newEntries = this.state.form.entries;

		let index = newEntries.findIndex((entry) => {
			return "CheckEntry" in entry && entry.CheckEntry.id === id
		});

		(newEntries[index] as CheckEntry).CheckEntry.value = value

		let newForm = this.state.form;
		newForm.entries = newEntries;
		this.state.form = newForm
	}

	private async onPathEntryClick(entry: PathEntry) {
		let newEntries = this.state.form.entries;

		let index = newEntries.findIndex((e) => {
			return "PathEntry" in e && e.PathEntry.id === entry.PathEntry.id;
		});

		if (entry.PathEntry.select_folder) {
			const folder_path = await open({
				directory: true,
				multiple: false,
			});

			if (folder_path) {
				console.log(folder_path);

				(newEntries[index] as PathEntry).PathEntry.value = folder_path;
			}
		} else {
			const file_path = await open({
				multiple: false,
				filters: [
					{ name: 'Supported Types', extensions: entry.PathEntry.file_extensions ?? [] },
				],
			});

			if (file_path) {
				(newEntries[index] as PathEntry).PathEntry.value = file_path;
			}
		}

		let newForm = this.state.form;
		newForm.entries = newEntries;

		this.state.form = newForm
	}

	private onClearPathClick(id: string) {
		let newEntries = this.state.form.entries;

		let index = newEntries.findIndex((entry) => {
			return "PathEntry" in entry && entry.PathEntry.id === id
		});

		(newEntries[index] as PathEntry).PathEntry.value = null

		let newForm = this.state.form;
		newForm.entries = newEntries;

		this.state.form = newForm
	}

	private onSelectEntryClick(id: string, value: string) {
		let newEntries = this.state.form.entries;

		let index = newEntries.findIndex((entry) => {
			return "SelectEntry" in entry && entry.SelectEntry.id === id
		});

		(newEntries[index] as SelectEntry).SelectEntry.value = value

		let newForm = this.state.form;
		newForm.entries = newEntries;

		this.state.form = newForm
	}

	private onKeydown(event: KeyboardEvent) {
		if (event.key === "Escape") {
			event.preventDefault();
			this.goBack();
		}

	}

	private onPrimaryButtonClick() {
		let message: FormSubmittedMessage = {
			plugin_id: this.state.form.plugin_id,
			results: this.state.form.entries.map(entry => {
				if ("TextEntry" in entry) {
					return {
						TextFormResult: {
							id: entry.TextEntry.id,
							value: entry.TextEntry.value,
							custom_info: entry.TextEntry.custom_info
						}
					}
				}

				if ("NumberEntry" in entry) {
					return {
						NumberFormResult: {
							id: entry.NumberEntry.id,
							value: entry.NumberEntry.value,
							custom_info: entry.NumberEntry.custom_info
						}
					}
				}

				if ("CheckEntry" in entry) {
					return {
						CheckFormResult: {
							id: entry.CheckEntry.id,
							value: entry.CheckEntry.value,
							custom_info: entry.CheckEntry.custom_info
						}
					}
				}

				if ("PathEntry" in entry) {

					return {
						PathFormResult: {
							id: entry.PathEntry.id,
							value: entry.PathEntry.value,
							custom_info: entry.PathEntry.custom_info
						}
					}
				}

				if ("SelectEntry" in entry) {
					return {
						SelectFormResult: {
							id: entry.SelectEntry.id,
							value: entry.SelectEntry.value,
							custom_info: entry.SelectEntry.custom_info
						}
					}
				}

				throw "Could not convert entry to form result"
			}),
			custom_info: this.state.form.custom_info
		}

		emit("send-form-to-plugin", { message: message });

		goto("/");
	}

	private goBack() {
		goto("/");
	}
}
