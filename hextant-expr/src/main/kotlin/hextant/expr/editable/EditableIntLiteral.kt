/**
 *@author Nikolaus Knop
 */

package hextant.expr.editable

import hextant.core.editable.EditableToken
import hextant.expr.edited.IntLiteral
import kserial.Serializable

class EditableIntLiteral() : EditableToken<IntLiteral>(), Serializable, EditableExpr<IntLiteral> {
    constructor(v: Int) : this() {
        text.set(v.toString())
    }

    constructor(v: IntLiteral) : this(v.value)

    override fun isValid(tok: String): Boolean = tok.toIntOrNull() != null

    override fun compile(tok: String): IntLiteral = IntLiteral(tok.toInt())
}