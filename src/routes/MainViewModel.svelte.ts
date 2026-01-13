import type { Settings } from "$lib/features/settings/Settings";
import { SettingsRepository } from "$lib/features/settings/SettingsRepository.svelte";
import { emit, listen } from "@tauri-apps/api/event";
import { getCurrentWindow } from "@tauri-apps/api/window";

export interface Entry {
	text: string,
	subtext: string | null,
	icon_path: string | null,
	custom_info: string[] | null
	action: Action
}

export type Action = OpenApp | RunOnPlugin | null;

export interface OpenApp {
	path: string
}

export interface RunOnPlugin {
	plugin_id: string;
	action: string;
	custom_indo: string[] | null
}

export type MainAction = { action: "type", searchText: string } | { action: "keydown", event: KeyboardEvent };

export class MainVM {
	state = $state<{
		html: string,
		searchText: string;
		selectionIndex: number;
		entries: Entry[]
	}>({
		html: "",
		searchText: "",
		selectionIndex: 0,
		entries: []
	});

	private searchDebounce: ReturnType<typeof setTimeout> | undefined;

	constructor() {
		this.addSettingsListener();
		this.addEntriesListener();
	}

	onAction(action: MainAction) {
		switch (action.action) {
			case "type": {
				this.onType(action.searchText);
				break;
			}

			case "keydown": {
				this.onKeydown(action.event);
				break;
			}
		}
	}

	private onKeydown(event: KeyboardEvent) {
		if (event.key === "ArrowDown") {
			event.preventDefault();
			this.onArrowDown();
		}

		if (event.key === "ArrowUp") {
			event.preventDefault();
			this.onArrowUp();
		}

		if (event.key === "Enter") {
			event.preventDefault();
			this.onEnter();
		}

		if (event.key === "Escape") {
			event.preventDefault();
			this.onEscape();
		}
	}


	// ---------------------------------------------------------------- //


	private addEntriesListener() {
		listen<Entry[]>("set-entries", (event) => {
			this.state.selectionIndex = 0;
			this.state.entries = event.payload;
		});
	}

	private addSettingsListener() {
		const settingsRepo = new SettingsRepository();

		settingsRepo.settingsCallback = (settings: Settings) => {
			this.state.html = settingsRepo.getHTML(settings);

			getCurrentWindow().show();
		}
	}


	// ---------------------------------------------------------------- //



	private onType(searchText: string) {
		this.state.searchText = searchText;

		clearTimeout(this.searchDebounce);

		this.searchDebounce = setTimeout(() => {
			emit("on-search", { text: searchText });
		}, 80);
	}

	private onArrowDown() {
		let newIndex = this.state.selectionIndex + 1;

		if (newIndex >= this.state.entries.length) {
			this.state.selectionIndex = 0;
			this.scrollIntoEntry(0, false);
			return;
		}

		this.state.selectionIndex = newIndex;

		this.scrollIntoEntry(newIndex, true);
	}

	private onArrowUp() {
		let newIndex = this.state.selectionIndex - 1;

		if (newIndex < 0) {
			newIndex = this.state.entries.length - 1;

			this.state.selectionIndex = newIndex;
			this.scrollIntoEntry(newIndex, true);
			return;
		}

		this.state.selectionIndex = newIndex

		this.scrollIntoEntry(newIndex, false);
	}

	private onEnter() {
		let entry = this.state.entries[this.state.selectionIndex];

		if (entry.action) {
			console.log(entry.action);
			emit("exec-action", { action: entry.action });

			this.resetScreen();
		}
	}

	private onEscape() {
		getCurrentWindow().close();

		this.resetScreen();
	}


	// ----------------------------------------------------------------------- //


	private scrollIntoEntry(index: number, goingDown: boolean) {
		const entriesDiv = document.getElementById("entries-div")!! as HTMLDivElement;
		const entryDiv = document.getElementById(`entry-${index}`)!! as HTMLDivElement;

		const entriesRect = entriesDiv.getBoundingClientRect();
		const entryRect = entryDiv.getBoundingClientRect();

		const isFullyVisible = goingDown ? entryRect.bottom <= entriesRect.bottom : entryRect.top >= entriesRect.top;

		if (!isFullyVisible) {
			entryDiv.scrollIntoView({
				block: goingDown ? "end" : "start"
			});
		}
	}

	private resetScreen() {
		this.state.searchText = "";
		this.state.selectionIndex = 0;
		this.state.entries = [];

		let input = document.getElementById("search-input") as HTMLInputElement;
		input.value = "";
	}
}
