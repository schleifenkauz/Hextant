/**
 * @author Nikolaus Knop
 */

package hextant.codegen

import hextant.Context
import hextant.Editor
import hextant.core.editor.ExpanderConfig
import hextant.core.editor.ExpanderDelegate

/**
 * A factory for [ExpanderDelegate]'s
 */
interface ExpanderDelegator<out E : Editor<*>> {
    fun getDelegate(): ExpanderDelegate<E>
}

abstract class ExpanderConfigurator<E : Editor<*>>(configure: ExpanderConfig<E>.() -> Unit) : ExpanderDelegator<E> {
    private val config = ExpanderConfig<E>().apply(configure)

    final override fun getDelegate(): ExpanderDelegate<E> = config
}

abstract class SimpleExpanderDelegator<E : Editor<*>>(private val function: (text: String, ctx: Context) -> E?) :
    ExpanderDelegator<E> {
    private val delegate = object : ExpanderDelegate<E> {
        override fun expand(text: String, context: Context): E? = function(text, context)
    }

    override fun getDelegate(): ExpanderDelegate<E> = delegate
}