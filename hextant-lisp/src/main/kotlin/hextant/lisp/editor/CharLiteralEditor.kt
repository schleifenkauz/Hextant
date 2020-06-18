/**
 * @author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.lisp.CharLiteral
import validated.*

class CharLiteralEditor(context: Context) : TokenEditor<CharLiteral, TokenEditorView>(context),
                                            SExprEditor<CharLiteral> {
    constructor(value: Char, context: Context) : this(context) {
        setText(value.toString())
    }

    constructor(value: CharLiteral, context: Context) : this(value.value, context)

    override fun compile(token: String): Validated<CharLiteral> =
        token.takeIf { token.length == 1 }.validated { invalid("Invalid string literal") }
            .map { CharLiteral(it.first()) }
}