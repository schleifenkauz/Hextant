/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.editor

import org.nikok.hextant.Editable
import org.nikok.hextant.core.editable.Expandable
import org.nikok.hextant.core.view.ExpanderView

open class ConfiguredExpander<E : Editable<*>>(
    private val config: ExpanderConfig<E>,
    view: ExpanderView,
    edited: Expandable<*, E>
): Expander<E>(view, edited) {
    override fun expand(text: String): E? = config.expand(text)
}