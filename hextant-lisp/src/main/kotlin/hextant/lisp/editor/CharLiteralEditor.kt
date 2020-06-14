/**
 * @author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.lisp.CharLiteral
import validated.*

class CharLiteralEditor(context: Context) : TokenEditor<CharLiteral>(context),
                                            SExprEditor<CharLiteral> {
    constructor(value: Char, context: Context) : this(context) {
        setText(value.toString())
    }

    constructor(value: CharLiteral, context: Context) : this(value.value, context)

    override fun compile(token: String): Validated<CharLiteral> =
        token.takeIf { token.length == 1 }.validated { invalid("Invalid string literal") }
            .map { CharLiteral(it.first()) }
}