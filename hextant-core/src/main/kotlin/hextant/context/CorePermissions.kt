package hextant.context

import bundles.Permission

/**
 * Internal permission only usable from the core module
 */
sealed class Internal : Permission("hextant.internal") {
    /**
     * Only instance of the [Internal] permission
     */
    internal companion object : Internal()
}