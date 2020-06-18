/**
 * @author Nikolaus Knop
 */

package hextant.context

import bundles.Bundle
import hextant.base.AbstractContext

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
        inline fun newInstance(
            parent: Context? = null,
            configure: Context.() -> Unit = {}
        ): Context = object : AbstractContext(parent = parent) {
        }.apply(configure)
    }
}