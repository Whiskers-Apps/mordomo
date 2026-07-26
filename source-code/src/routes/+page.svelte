<script lang="ts">
	import { convertFileSrc } from "@tauri-apps/api/core";
	import { MainVM } from "./MainViewModel.svelte";

	// Icons
	import SearchIcon from "$lib/icons/search.svg?component";
	import GlobeIcon from "$lib/icons/globe.svg?component";
	let vm = new MainVM();
</script>

<svelte:window
	on:keydown={(e) => {
		vm.onAction({ action: "keydown", event: e });
	}}
/>

<div class="text-main">
	{@html vm.state.html}

	<div class="flex flex-col h-screen bg-main">
		<div class="flex items-center p-5">
			<!-- <SearchIcon class="h-6 w-6" /> -->
			<!-- svelte-ignore a11y_autofocus -->
			<input
				id="search-input"
				class="w-full ml-2 outline-none placeholder"
				value={vm.state.searchText}
				autofocus
				placeholder="Search"
				oninput={(e) => {
					let text = (e.target as HTMLInputElement)!!.value;

					vm.onAction({
						action: "type",
						searchText: text,
					});
				}}
			/>
		</div>

		<div
			id="entries-div"
			class="mr-4 ml-4 mb-4 space-y-2 overflow-auto flex-1"
		>
			{#each vm.state.entries as entry, index}
				<button
					id={`entry-${index}`}
					class={`flex hover-bg-secondary w-full p-2 rounded-lg items-center ${vm.state.selectionIndex === index ? "bg-secondary" : ""}`}
					onclick={() => {}}
				>
					{#if entry.icon_path}
						<img
							class="object-contain rounded-lg mr-4"
							src={convertFileSrc(entry.icon_path)}
							height="24"
							width="24"
							alt="Icon"
						/>
					{:else if entry.custom_info?.includes("{icon}-globe")}
						<GlobeIcon class="mr-4" height="24" width="24" />
					{/if}

					<div class="grow text-start">
						<div>
							{entry.text}
						</div>

						<div class="text-xs text-secondary">
							{#if entry.subtext}
								{entry.subtext}
							{/if}
						</div>
					</div>
				</button>
			{/each}
		</div>
	</div>
</div>
