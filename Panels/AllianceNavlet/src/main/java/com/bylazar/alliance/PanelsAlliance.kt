/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Panel plugin managing alliance display navlet
   ------------------------------------------------------- */
package com.bylazar.alliance

object PanelsAlliance {
    // @JvmStatic : accessible from Java as PanelsAlliance.getProvider()
    @JvmStatic
    val provider: AllianceProvider
        get() = Plugin.provider
}
