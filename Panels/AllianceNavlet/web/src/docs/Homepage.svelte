<script lang="ts">
    import type {PluginConfig} from "ftc-panels"
    import {Title, Paragraph, AutoplayVideo, Accent, CodeBlock} from "ftc-panels/docs"

    let {
        info,
    }: {
        info: PluginConfig
    } = $props()

</script>

<Title level={2}>Alliance Navlet</Title>

<Paragraph>
    This shows the current <Accent>alliance color</Accent>, providing a quick color coded visual reference.
</Paragraph>

<Title level={2}>Alliance Reading</Title>

<CodeBlock code={code}></CodeBlock>

