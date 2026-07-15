<script lang="ts">
	import { SettingsVM } from "./SettingsVM.svelte";
	import BackIcon from "$lib/icons/back.svg?component";

	let vm = new SettingsVM();
</script>

<svelte:window
	on:keydown={(e) => {
		vm.onIntent({ intent: "keydown", event: e });
	}}
/>

<div class="text-main bg-main h-screen p-4">
	{@html vm.state.html}

	<div class="flex items-center">
		<button
			class="bg-secondary p-2 rounded-full hover-tertiary"
			onclick={() => {
				vm.onIntent({ intent: "back" });
			}}
		>
			<BackIcon class="h-5 w-5" />
		</button>

		<div class="w-4"></div>

		<p class="text-lg">Settings</p>
	</div>
</div>
