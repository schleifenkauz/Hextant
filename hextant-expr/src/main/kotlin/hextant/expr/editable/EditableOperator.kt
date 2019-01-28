package hextant.expr.editable

import hextant.core.editable.EditableToken
import hextant.expr.edited.Operator
import kserial.Serializable

class EditableOperator() : EditableToken<Operator>(), Serializable {
    constructor(operator: Operator) : this() {
        text.set(operator.name)
    }

    override fun isValid(tok: String): Boolean = Operator.isValid(tok)

    override fun compile(tok: String): Operator = Operator.of(tok)
}
