/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.*
import hextant.base.ParentEditor
import hextant.core.editable.Expandable
import hextant.core.view.ExpanderView
import hextant.undo.*
import reaktive.value.now

abstract class Expander<E : Editable<*>, out Ex : Expandable<*, E>>(
    editable: Ex,
    private val context: Context
) : ParentEditor<Ex, ExpanderView>(editable, context) {
    private val undo = context[UndoManager]

    override fun viewAdded(view: ExpanderView) {
        view.onGuiThread {
            if (editable.isExpanded.now) {
                view.expanded(editable.editable.now!!)
            } else {
                view.reset()
            }
        }
    }

    protected abstract fun expand(text: String): E?

    fun expand() {
        context.runLater {
            check(!editable.isExpanded.now) { "Expander is already expanded" }
            val content = expand(editable.text.now) ?: return@runLater
            val edit = expandTo(content)
            undo.push(edit)
        }
    }

    private fun expandTo(content: E): Edit {
        val text = editable.text.now
        setContent(content)
        return ExpandEdit(text, content)
    }

    private inner class ExpandEdit(private val oldText: String, private val content: E) : AbstractEdit() {
        override fun doRedo() {
            expandTo(content)
        }

        override fun doUndo() {
            doReset()
            doSetText(oldText)
        }

        override val actionDescription: String
            get() = "Expand $oldText"
    }

    fun reset() {
        context.runLater {
            check(editable.isExpanded.now) { "Expander is not expanded" }
            val edit = doReset()
            undo.push(edit)
        }
    }

    private fun doReset(): Edit {
        val oldContent = editable.editable.now!!
        val editor = context.getEditor(oldContent)
        editor.moveTo(null)
        editable.setText("")
        views { reset() }
        return ResetEdit(oldContent)
    }

    private inner class ResetEdit(private val oldContent: E) : AbstractEdit() {
        override fun doRedo() {
            doReset()
        }

        override fun doUndo() {
            expandTo(oldContent)
        }

        override val actionDescription: String
            get() = "Reset Expander"
    }

    fun setText(new: String) {
        context.runLater {
            check(!editable.isExpanded.now) { "Cannot set text while expander is expanded" }
            val edit = doSetText(new)
            undo.push(edit)
        }
    }

    private fun doSetText(new: String): Edit {
        val old = editable.text.now
        editable.setText(new)
        views {
            textChanged(new)
        }
        return SetTextEdit(this, old, new)
    }

    private class SetTextEdit(private val expander: Expander<*, *>, private val old: String, private val new: String) :
        AbstractEdit() {
        override fun doRedo() {
            expander.doSetText(new)
        }

        override fun doUndo() {
            expander.doSetText(old)
        }

        override val actionDescription: String
            get() = "Typing"

        override fun mergeWith(other: Edit): Edit? {
            if (other !is SetTextEdit) return null
            if (other.expander !== this.expander) return null
            return SetTextEdit(expander, this.old, other.new)
        }
    }

    fun setContent(new: E) {
        editable.setContent(new)
        val editor = context.getEditor(new)
        editor.moveTo(this)
        views { expanded(new) }
    }

    override fun extendSelection(child: Editor<*>) {
        parent?.extendSelection(child)
    }
}