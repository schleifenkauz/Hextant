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
    view: ExpanderView, editable: Expandable<*, E>
) : AbstractEditor<Expandable<*, @UnsafeVariance E>>(editable, view) {
    init {
        handleState(editable.isExpanded.now, view, editable)
    }

    private val isExpandedObserver = editable.isExpanded.observe("Observe if $editable is expanded") { expanded ->
        handleState(expanded, view, editable)
    }

    private fun handleState(
        expanded: Boolean, view: ExpanderView, expandable: Expandable<*, E>
    ) {
        if (expanded) {
            view.expanded(expandable.editable.now!!)
        } else {
            view.reset()
        }
    }

    private val textObserver = editable.text.observe("Observe text of $editable") { t ->
        view.textChanged(t)
    }

    protected abstract fun expand(text: String): E?

    fun expand(): Boolean {
        check(!editable.isExpanded.now) { "Expander is already expanded" }
        val content = expand(editable.text.now) ?: return false
        editable.setContent(content)
        return true
    }

    fun reset() {
        check(editable.isExpanded.now) { "Expander is not expanded" }
        editable.setText("")
    }
}