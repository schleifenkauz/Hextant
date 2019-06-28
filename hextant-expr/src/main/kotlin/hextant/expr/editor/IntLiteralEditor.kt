/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.*
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.expr.edited.IntLiteral

class IntLiteralEditor(
    context: Context,
    text: String
) : TokenEditor<IntLiteral, TokenEditorView>(context, text), ExprEditor<IntLiteral> {
    constructor(context: Context) : this(context, "")

    constructor(v: IntLiteral, context: Context) : this(context, v.value.toString())

    override fun compile(token: String): CompileResult<IntLiteral> =
        token.toIntOrNull().okOrErr { "Invalid int literal $token" }.map { IntLiteral(it) }
}