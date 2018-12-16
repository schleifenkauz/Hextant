/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editor

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.editor.TokenEditor
import org.nikok.hextant.core.expr.editable.EditableIntLiteral
import org.nikok.hextant.core.expr.edited.Expr
import org.nikok.hextant.core.expr.view.IntLiteralEditorView
import org.nikok.reaktive.value.now

class IntLiteralEditor(
    editable: EditableIntLiteral,
    platform: HextantPlatform
) : TokenEditor<EditableIntLiteral, IntLiteralEditorView>(editable, platform), ExprEditor {
    override val expr: Expr?
        get() = editable.edited.now
}