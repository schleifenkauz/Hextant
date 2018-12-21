/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.core.editable.Expandable
import hextant.view.ExpanderView
import org.nikok.reaktive.value.now

abstract class Expander<E : Editable<*>>(
    editable: Expandable<*, E>,
    private val context: Context
) : AbstractEditor<Expandable<*, E>, ExpanderView>(editable, context) {
    private fun ExpanderView.handleState() {
        if (editable.isExpanded.now) {
            expanded(editable.editable.now!!)
        } else {
            reset()
        }
    }

    override fun viewAdded(view: ExpanderView) {
        view.onGuiThread {
            view.handleState()
        }
    }

    protected abstract fun expand(text: String): E?

    fun expand() {
        context.runLater {
            check(!editable.isExpanded.now) { "Expander is already expanded" }
            val content = expand(editable.text.now) ?: return@runLater
            setContent(content)
        }
    }

    fun reset() {
        context.runLater {
            check(editable.isExpanded.now) { "Expander is not expanded" }
            editable.setText("")
            notifyViews()
        }
    }

    fun setText(new: String) {
        context.runLater {
            editable.setText(new)
            views {
                textChanged(new)
            }
        }
    }

    fun setContent(new: E) {
        editable.setContent(new)
        notifyViews()
    }

    override fun extendSelection(child: Editor<*>) {
        parent?.extendSelection(child)
    }

    private fun notifyViews() {
        views {
            handleState()
        }
    }
}