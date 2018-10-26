package org.nikok.hextant.core

import org.nikok.hextant.prop.Permission

/**
 * The permissions of the core module
*/
object CorePermissions {
    /**
     * Internal permission only usable from the core module
    */
    sealed class Internal : Permission() {
        companion object : Internal()
    }

    /**
     * Public permission usable by everybody
    */
    object Public : Permission()
}