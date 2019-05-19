/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.*
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.expr.edited.IntLiteral

class IntLiteralEditor(context: Context) : TokenEditor<IntLiteral, TokenEditorView>(context), ExprEditor<IntLiteral> {
    constructor(v: Int, context: Context) : this(context) {
        setText(v.toString())
    }

    constructor(v: IntLiteral, context: Context) : this(v.value, context)

    override fun compile(token: String): CompileResult<IntLiteral> =
        token.toIntOrNull().okOrErr { "Invalid int literal $token" }.map { IntLiteral(it) }
}