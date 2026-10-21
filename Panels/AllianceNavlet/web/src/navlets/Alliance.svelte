<script lang="ts">
    import { onMount } from "svelte"
    import type Manager from "../manager"

    const isDev = false

    let { manager }: { manager: Manager } = $props()

    let color = $state("NONE")

    onMount(() => {
        manager.state.onChange(manager.ALLIANCE_KEY, (data: number) => {
            color = data
        })

        if(!isDev) return
        color = "NONE"

    })
</script>

<p>
    <span
        class="alliance-container"
        class:red={color === "RED"}
        class:blue={color === "BLUE"}
    >
        <span class="alliance-text">ALLIANCE</span>
    </span>
</p>

<style>
    p {
        white-space: nowrap;
        margin: 0;
    }

    .alliance-container {
        display: inline-block;
        padding: 4px 8px;
        border-radius: 4px;
        position: relative;
    }
    .alliance-container.red {
        background-color: red;
    }
    .alliance-container.blue {
         background-color: blue;
    }
    .alliance-text {
        position: relative;
        z-index: 2;
        padding-left: 6px;
        padding-right: 6px;
        font-weight: bold;
    }
</style>
