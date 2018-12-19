/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editor

import org.nikok.hextant.Context
import org.nikok.hextant.core.editor.TokenEditor
import org.nikok.hextant.core.expr.editable.EditableIntLiteral
import org.nikok.hextant.core.expr.edited.Expr
import org.nikok.hextant.core.expr.view.IntLiteralEditorView
import org.nikok.reaktive.value.now

class IntLiteralEditor(
    editable: EditableIntLiteral,
    context: Context
) : TokenEditor<EditableIntLiteral, IntLiteralEditorView>(editable, context), ExprEditor {
    override val expr: Expr?
        get() = editable.edited.now
}