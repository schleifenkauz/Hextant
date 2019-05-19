/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.*
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.lisp.DoubleLiteral

class DoubleLiteralEditor(context: Context) : TokenEditor<DoubleLiteral, TokenEditorView>(context),
                                              SExprEditor<DoubleLiteral> {
    constructor(value: Double, context: Context) : this(context) {
        setText(value.toString())
    }

    constructor(value: DoubleLiteral, context: Context) : this(value.value, context)

    override fun compile(token: String): CompileResult<DoubleLiteral> =
        token.toDoubleOrNull().okOrErr { "Invalid double literal $token" }.map(::DoubleLiteral)
}