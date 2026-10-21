import type { PluginConfig } from "ftc-panels"

export const config: PluginConfig = {
  id: "com.bylazar.alliance",
  name: "Alliance",
  letterName: "A",
  description: "Alliance Utils for Panels",
  websiteURL: "https://github.com/MantaBots27318/core",
  packageString: "com.bylazar:alliance:<VERSION>",
  version: "1.0.0",
  pluginsCoreVersion: "1.1.44",
  author: "Lazar",
  manager: "src/manager.ts",
  components: [
    {
      type: "navlet",
      id: "Alliance",
      filepath: "src/navlets/Alliance.svelte",
    },
    {
      type: "docs",
      id: "Homepage",
      filepath: "src/docs/Homepage.svelte",
    },
  ],
  templates: [],
  includedPluginsIDs: [],
  changelog: [
    {
      version: "1.0.0",
      release_date: "22.03.2026",
      changes: [
        {
          type: "other",
          description: "First release",
          upgrading: "",
        },
      ],
    },
  ],
}
