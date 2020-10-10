/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.core.Editor

/**
 * A factory for [ExpanderDelegate]'s
 */
interface ExpanderDelegator<out E : Editor<*>> {
    fun getDelegate(): ExpanderDelegate<E, Context>
}

abstract class ExpanderConfigurator<E : Editor<*>>(configure: ExpanderConfig<E, Context>.() -> Unit) :
    ExpanderDelegator<E> {
    val config = ExpanderConfig<E, Context>().apply(configure)

    final override fun getDelegate(): ExpanderConfig<E, Context> = config
}

abstract class SimpleExpanderDelegator<E : Editor<*>>(private val function: (text: String, ctx: Context) -> E?) :
    ExpanderDelegator<E> {
    private val delegate = object : ExpanderDelegate<E, Context> {
        override fun expand(text: String, context: Context): E? = function(text, context)

        override fun expand(item: Any, context: Context): E? = null
    }

    override fun getDelegate(): ExpanderDelegate<E, Context> = delegate
}