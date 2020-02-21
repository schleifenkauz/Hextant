package hextant.bundle

/**
 * Internal permission only usable from the core module
 */
sealed class Internal {
    /**
     * Only instance of the [Internal] permission
     */
    internal companion object : Internal()
}