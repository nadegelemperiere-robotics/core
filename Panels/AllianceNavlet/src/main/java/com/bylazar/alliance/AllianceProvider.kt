/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Panel plugin provider managing alliance display navlet
   ------------------------------------------------------- */
package com.bylazar.alliance

// System includes
import java.util.function.Supplier


class AllianceProvider {

    // Alliance color source, registered by TeamCode (the plugin can't depend on TeamCode)
    var colorSupplier: Supplier<String?> = Supplier { null }

    // Last read alliance color
    var color: String = "NONE"
        private set

    /** Reads the current alliance color, returns it if it changed since last call, null otherwise */
    fun update(): String? {
        val previous = color
        color = colorSupplier.get() ?: "NONE"
        return if (color != previous) color else null
    }
}
