/**
 * @author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.*
import hextant.core.editable.EditableToken
import hextant.lisp.CharLiteral

class EditableCharLiteral() : EditableToken<CharLiteral>(), EditableSExpr<CharLiteral> {
    constructor(value: Char) : this() {
        text.set(value.toString())
    }

    constructor(value: CharLiteral) : this(value.value)

    override fun compile(tok: String): CompileResult<CharLiteral> =
        tok.okIfOrErr(tok.length == 1) { "Invalid string literal" }.map { CharLiteral(it.first()) }
}