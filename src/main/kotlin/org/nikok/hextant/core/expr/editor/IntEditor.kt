/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editor

import org.nikok.hextant.core.base.AbstractEditor
import org.nikok.hextant.core.expr.editable.EditableIntLiteral
import org.nikok.hextant.core.expr.edited.Expr
import org.nikok.hextant.core.expr.view.IntLiteralEditorView
import org.nikok.reaktive.value.now
import org.nikok.reaktive.value.observe

class IntEditor(
    editable: EditableIntLiteral,
    view: IntLiteralEditorView
): AbstractEditor<EditableIntLiteral>(editable, view), ExprEditor {
    override val expr: Expr?
        get() = editable.edited.now

    init {
        val text = editable.text.now
        view.textChanged(text)
    }

    private val textObserver = editable.text.observe("Observe text for view", view::textChanged)

    fun dispose() {
        textObserver.kill()
    }

    fun setValue(text: String) {
        editable.text.set(text)
    }
}