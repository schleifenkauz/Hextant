/**
 * @author Nikolaus Knop
 */

package hextant

import hextant.base.AbstractContext
import hextant.bundle.Bundle

/**
 * A context is used for passing down dependencies used by editors and views
 */
interface Context : Bundle {
    /**
     * The platform instance is the root of the context hierarchy
     */
    val platform: HextantPlatform

    /**
     * The parent context, properties not found in this [Context] are searched for in the parent context
     */
    val parent: Context?

    companion object {
        /**
         * Create a new [Context] using the specified [parent]. Before returning execute the [configure] block.
         */
        inline fun newInstance(
            parent: Context = HextantPlatform.configured(),
            configure: Context.() -> Unit = {}
        ): Context = object : AbstractContext(parent = parent) {
            override val platform: HextantPlatform
                get() = parent.platform
        }.apply(configure)
    }
}