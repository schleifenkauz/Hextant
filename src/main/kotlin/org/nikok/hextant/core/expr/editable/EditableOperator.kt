package org.nikok.hextant.core.expr.editable

import org.nikok.hextant.Editable
import org.nikok.hextant.core.expr.edited.Operator
import org.nikok.reaktive.value.ReactiveBoolean
import org.nikok.reaktive.value.ReactiveValue

class EditableOperator(override val parent: Editable<*>? = null) : Editable<Operator> {
    constructor(initial: Operator, parent: Editable<*>? = null): this(parent) {
        editableText.text.set(initial.name)
    }

    val editableText = EditableText()

    override val edited: ReactiveValue<Operator?> = editableText.text.map("operator of $this") {
        Operator.of(it)
    }
    override val isOk: ReactiveBoolean = edited.map("$this is ok") {
        it != null
    }
}
