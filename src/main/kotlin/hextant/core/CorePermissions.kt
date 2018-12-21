package hextant.core

import hextant.bundle.Permission

/**
 * The permissions of the core module
 */
object CorePermissions {
    /**
     * Internal permission only usable from the core module
     */
    sealed class Internal : Permission() {
        /**
         * Only instance of the [Internal] permission
         */
        internal companion object : Internal()
    }

    /**
     * Public permission usable by everybody
     */
    object Public : Permission()
}