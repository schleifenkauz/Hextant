package hextant.expr.editable

import hextant.*
import hextant.core.editable.EditableToken
import hextant.expr.edited.Operator
import kserial.Serializable

class EditableOperator() : EditableToken<Operator>(), Serializable {
    constructor(operator: Operator) : this() {
        text.set(operator.name)
    }

    override fun compile(tok: String): CompileResult<Operator> =
        tok.takeIf { Operator.isValid(it) }.okOrErr { "Invalid operator $tok" }.map { Operator.of(it) }
}
