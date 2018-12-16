package org.nikok.hextant.core.expr.editable

import kserial.Serializable
import org.nikok.hextant.core.editable.EditableToken
import org.nikok.hextant.core.expr.edited.Operator

class EditableOperator() : EditableToken<Operator>(), Serializable {
    constructor(initial: Operator) : this() {
        text.set(initial.name)
    }

    override fun isValid(tok: String): Boolean = Operator.isValid(tok)

    override fun compile(tok: String): Operator = Operator.of(tok)
}
