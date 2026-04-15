<script lang="ts">
	import { FormVM } from "./FormViewModel.svelte";
	import BackIcon from "$lib/icons/back.svg?component";
	import FolderIcon from "$lib/icons/folder.svg?component";
	import TrashIcon from "$lib/icons/trash.svg?component";
	import DownIcon from "$lib/icons/chevron-down.svg?component";
	import { event } from "@tauri-apps/api";

	let vm = new FormVM();
</script>

<svelte:window
	on:keydown={(e) => {
		vm.onIntent({ intent: "keydown", event: e });
	}}
/>

{#if !vm.state.loading}
	{@html vm.state.html}

	<div class="text-main bg-main p-5 h-screen flex flex-col">
		<div class="flex items-center">
			<button
				class="bg-secondary p-2 rounded-full hover-tertiary"
				onclick={() => {
					vm.onIntent({ intent: "backClick" });
				}}
			>
				<BackIcon class="h-5 w-5" />
			</button>

			<div class="w-2"></div>

			<p class="text-lg">{vm.state.form.title}</p>
		</div>

		<div class="h-4"></div>

		<div class="space-y-4 flex-1 overflow-auto">
			{#each vm.state.form.entries as entry}
				{#if "TextEntry" in entry}
					<div>
						<p>{entry.TextEntry.title}</p>
						<p>{entry.TextEntry.description}</p>

						<div class="h-2"></div>

						<input
							class="border-secondary focus-border-text-main border p-3 rounded-xl w-full"
							value={entry.TextEntry.value}
							oninput={(e) => {
								vm.onIntent({
									intent: "text_entry_type",
									id: entry.TextEntry.id,
									value: (e.target as HTMLInputElement).value,
								});
							}}
						/>
					</div>
				{/if}

				{#if "NumberEntry" in entry}
					<div>
						<p>{entry.NumberEntry.title}</p>
						<p>{entry.NumberEntry.description}</p>

						<div class="h-2"></div>

						<input
							class="border-secondary focus-border-text-main border p-3 rounded-xl w-full"
							value={entry.NumberEntry.value}
							oninput={(e) => {
								const input = e.target as HTMLInputElement;
								const numberRegex = /^-?\d+(\.\d+)?$/;
								const value = input.value;

								if (
									value.trim() !== "" &&
									!numberRegex.test(value)
								) {
									input.value = String(
										entry.NumberEntry.value,
									);
									return;
								}

								vm.onIntent({
									intent: "number_entry_type",
									id: entry.NumberEntry.id,
									value:
										value.trim() === "" ? 0 : Number(value),
								});
							}}
						/>
					</div>
				{/if}

				{#if "CheckEntry" in entry}
					<div class="flex">
						<div class="flex-1">
							<p>{entry.CheckEntry.title}</p>
							<p>{entry.CheckEntry.description}</p>
						</div>

						<div class="w-2"></div>

						<label class="toggle">
							<input
								type="checkbox"
								checked={entry.CheckEntry.value}
								onchange={(e) =>
									vm.onIntent({
										intent: "check_entry_check",
										id: entry.CheckEntry.id,
										value: (e.target as HTMLInputElement)
											.checked,
									})}
							/>
							<span class="slider"></span>
						</label>
					</div>
				{/if}

				{#if "PathEntry" in entry}
					<div>
						<!-- {JSON.stringify(entry.PathEntry)} -->
						<div>
							<p>{entry.PathEntry.title}</p>
							<p class="text-t1 text-sm">
								{entry.PathEntry.description}
							</p>
						</div>

						<div class="h-2"></div>

						<div
							role="button"
							tabindex="0"
							class="border border-secondary p-3 rounded-xl flex w-full items-center"
							onclick={() => {
								vm.onIntent({
									intent: "path_entry_click",
									entry: entry,
								});
							}}
						>
							<FolderIcon class="h-5 w-5 shrink-0" />

							<div class="w-4 shrink-0"></div>

							{#if entry.PathEntry.value}
								<p class="flex-1 text-start truncate">
									{entry.PathEntry.value}
								</p>

								<div class="w-4 shrink-0"></div>

								<div
									role="button"
									tabindex="1"
									class="shrink-0"
									onclick={(e) => {
										e.stopPropagation();

										vm.onIntent({
											intent: "clear_path_click",
											id: entry.PathEntry.id,
										});
									}}
								>
									<TrashIcon class="h-5 w-5" />
								</div>
							{:else}
								<p class="flex-1 text-start">Select file</p>
							{/if}
						</div>
					</div>
				{/if}

				{#if "SelectEntry" in entry}
					<div>
						<p>{entry.SelectEntry.title}</p>
						<p class="text-t1 text-sm">
							{entry.SelectEntry.description}
						</p>
					</div>

					<div class="relative w-full">
						<select
							class="w-full bg-main border border-secondary p-3 rounded-xl focus-border-text-main appearance-none pr-6"
							bind:value={entry.SelectEntry.value}
							onchange={(e) => {
								vm.onIntent({
									intent: "select_entry_click",
									id: entry.SelectEntry.id,
									value: entry.SelectEntry.value,
								});
							}}
						>
							{#each entry.SelectEntry.options as option}
								<option value={option.id}>{option.text}</option>
							{/each}
						</select>

						<DownIcon
							class="w-5 h-5 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-foreground"
						/>
					</div>
				{/if}
			{/each}
		</div>

		<div class="h-4"></div>

		<div class="flex justify-end w-full">
			<button
				class="p-2 pl-4 pr-4 bg-accent text-on-accent hover:opacity-90 rounded-full"
				onclick={() => {
					vm.onIntent({ intent: "primaryButtonClick" });
				}}
			>
				{vm.state.form.positive_button_text}
			</button>
		</div>
	</div>
{/if}
