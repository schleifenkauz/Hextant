package hextant.core.expr.editable

import hextant.core.editable.EditableToken
import hextant.core.expr.edited.Operator
import kserial.Serializable

class EditableOperator : EditableToken<Operator>(), Serializable {
    override fun isValid(tok: String): Boolean = Operator.isValid(tok)

    override fun compile(tok: String): Operator = Operator.of(tok)
}
