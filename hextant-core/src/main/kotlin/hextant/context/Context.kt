/**
 * @author Nikolaus Knop
 */

package hextant.context

import bundles.Bundle

/**
 * A context is used for passing down dependencies used by editors and views
 */
interface Context : Bundle {
    /**
     * The parent context, properties not found in this [Context] are searched for in the parent context
     */
    val parent: Context?

    companion object {
        /**
         * Create a new [Context] using the specified [parent]. Before returning execute the [configure] block.
         */
        inline fun create(
            parent: Context? = null,
            configure: Context.() -> Unit = {}
        ): Context = ContextImpl(parent).apply(configure)
    }
}