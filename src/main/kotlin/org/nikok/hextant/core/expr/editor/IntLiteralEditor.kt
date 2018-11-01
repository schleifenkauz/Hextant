/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editor

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.base.AbstractEditor
import org.nikok.hextant.core.expr.editable.EditableIntLiteral
import org.nikok.hextant.core.expr.edited.Expr
import org.nikok.hextant.core.expr.view.IntLiteralEditorView
import org.nikok.reaktive.value.now

class IntLiteralEditor(
    editable: EditableIntLiteral,
    private val platform: HextantPlatform
): AbstractEditor<EditableIntLiteral, IntLiteralEditorView>(editable, platform), ExprEditor {
    override val expr: Expr?
        get() = editable.edited.now

    override fun viewAdded(view: IntLiteralEditorView) {
        view.onGuiThread { view.textChanged(editable.text.now) }
    }

    fun setValue(text: String) {
        platform.runLater { editable.text.set(text) }
        views { textChanged(text) }
    }
}