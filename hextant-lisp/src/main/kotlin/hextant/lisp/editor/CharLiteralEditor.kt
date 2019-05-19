/**
 * @author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.*
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.lisp.CharLiteral

class CharLiteralEditor(context: Context) : TokenEditor<CharLiteral, TokenEditorView>(context),
                                            SExprEditor<CharLiteral> {
    constructor(value: Char, context: Context) : this(context) {
        setText(value.toString())
    }

    constructor(value: CharLiteral, context: Context) : this(value.value, context)

    override fun compile(token: String): CompileResult<CharLiteral> =
        token.takeIf { token.length == 1 }.okOrErr { "Invalid string literal" }.map { CharLiteral(it.first()) }
}