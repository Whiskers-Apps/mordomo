<script lang="ts">
	import { emit, listen } from "@tauri-apps/api/event";
	import { onMount } from "svelte";

	let name = $state("");
	let entries = $state<Entry[]>([]);

	function greet() {
		emit("on-search", {
			text: name,
		});
	}

	interface Entry {
		text: string;
	}

	onMount(() => {
		listen<Entry[]>("set-entries", (event) => {
			entries = event.payload;
		});
	});
</script>

<main class="container">
	<input
		id="greet-input"
		placeholder="Enter a name..."
		bind:value={name}
		oninput={(e) => {
			greet();
		}}
	/>
	<button type="submit" onclick={() => greet()}>Greet</button>

	{#each entries as entry}
		<div>{entry.text}</div>
	{/each}
</main>

<style>
</style>
