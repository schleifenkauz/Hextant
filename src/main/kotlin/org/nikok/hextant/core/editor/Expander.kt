/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.editor

import org.nikok.hextant.Editable
import org.nikok.hextant.core.base.AbstractEditor
import org.nikok.hextant.core.editable.Expandable
import org.nikok.hextant.core.view.ExpanderView
import org.nikok.reaktive.value.now
import org.nikok.reaktive.value.observe

abstract class Expander<out E : Editable<*>>(
    editable: Expandable<*, E>
) : AbstractEditor<Expandable<*, @UnsafeVariance E>, ExpanderView>(editable) {
    init {
        handleState(editable.isExpanded.now)
    }

    private fun handleState(
        expanded: Boolean
    ) {
        if (expanded) {
            views { expanded(editable.editable.now!!) }
        } else {
            views { reset() }
        }
    }

    private val textObserver = editable.text.observe("Observe text of $editable") { t ->
        views { textChanged(t) }
    }

    protected abstract fun expand(text: String): E?

    fun expand(): Boolean {
        check(!editable.isExpanded.now) { "Expander is already expanded" }
        val content = expand(editable.text.now) ?: return false
        editable.setContent(content)
        handleState(expanded = true)
        return true
    }

    fun reset() {
        check(editable.isExpanded.now) { "Expander is not expanded" }
        editable.setText("")
        handleState(expanded = false)
    }
}