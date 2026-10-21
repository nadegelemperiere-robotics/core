import { PluginManager, getLazarPackageLatestVersion } from "ftc-panels"
import { config } from "../config"

export default class Manager extends PluginManager {
  ALLIANCE_KEY = "alliance"
  override onInit(): void {
    this.state.update(this.ALLIANCE_KEY, "NONE")
    this.socket.addMessageHandler("alliance", (data) => {
      this.state.update(this.ALLIANCE_KEY, data)
    })
  }

  static async getNewVersion(): Promise<string> {
    return await getLazarPackageLatestVersion(config.id)
  }
}
