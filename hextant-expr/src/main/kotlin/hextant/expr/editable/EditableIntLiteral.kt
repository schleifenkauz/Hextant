/**
 *@author Nikolaus Knop
 */

package hextant.expr.editable

import hextant.*
import hextant.core.editable.EditableToken
import hextant.expr.edited.IntLiteral
import kserial.Serializable

class EditableIntLiteral() : EditableToken<IntLiteral>(), Serializable, EditableExpr<IntLiteral> {
    constructor(v: Int) : this() {
        text.set(v.toString())
    }

    constructor(v: IntLiteral) : this(v.value)

    override fun compile(tok: String): CompileResult<IntLiteral> =
        tok.toIntOrNull().okOrErr { "Invalid int literal $tok" }.map { IntLiteral(it) }
}