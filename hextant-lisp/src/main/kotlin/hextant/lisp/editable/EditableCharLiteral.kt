/**
 * @author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.core.editable.EditableToken
import hextant.lisp.CharLiteral

class EditableCharLiteral() : EditableToken<CharLiteral>(), EditableSExpr<CharLiteral> {
    constructor(value: Char) : this() {
        text.set(value.toString())
    }

    override fun isValid(tok: String): Boolean = tok.length == 1

    override fun compile(tok: String): CharLiteral = CharLiteral(tok.first())
}