/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.core.editable.EditableToken
import hextant.lisp.IntLiteral

class EditableIntLiteral() : EditableToken<IntLiteral>(), EditableSExpr<IntLiteral> {
    constructor(value: Int) : this() {
        text.set(value.toString())
    }

    override fun isValid(tok: String): Boolean = tok.toIntOrNull() != null

    override fun compile(tok: String): IntLiteral = IntLiteral(tok.toInt())
}