/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.Context
import hextant.Editor

/**
 * An [Expander] which expands text using its [config]
 */
abstract class ConfiguredExpander<R : Any, E : Editor<R>>(
    private val config: ExpanderConfig<E>,
    context: Context
) : Expander<R, E>(context) {
    override fun expand(text: String): E? = config.expand(text, context)
}