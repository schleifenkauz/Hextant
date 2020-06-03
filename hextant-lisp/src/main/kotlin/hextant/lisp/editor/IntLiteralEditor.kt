/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.lisp.IntLiteral
import validated.*

class IntLiteralEditor(context: Context) : TokenEditor<IntLiteral, TokenEditorView>(context), SExprEditor<IntLiteral> {
    constructor(value: Int, context: Context) : this(context) {
        setText(value.toString())
    }

    constructor(value: IntLiteral, context: Context) : this(value.value, context)

    override fun compile(token: String): Validated<IntLiteral> =
        token.toIntOrNull().validated { invalid("Invalid int literal $token") }.map(::IntLiteral)
}