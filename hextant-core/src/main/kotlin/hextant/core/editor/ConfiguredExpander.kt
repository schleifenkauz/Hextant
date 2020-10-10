/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.core.Editor

/**
 * An [Expander] which expands text using its [config]
 */
abstract class ConfiguredExpander<R, E : Editor<R>>(
    private val config: ExpanderDelegate<E, Context>,
    context: Context,
    editor: E? = null
) : Expander<R, E>(context, editor) {
    override fun expand(text: String): E? = config.expand(text, context)

    override fun expand(completion: Any): E? = config.expand(completion, context)
}