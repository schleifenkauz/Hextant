/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.Context
import hextant.Editable
import hextant.core.editable.Expandable

open class ConfiguredExpander<E : Editable<*>>(
    private val config: ExpanderConfig<E>,
    edited: Expandable<*, E>,
    context: Context
) : Expander<E>(edited, context) {
    @Suppress("UNCHECKED_CAST")
    override fun expand(text: String): E? = config.expand(text)
}