/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editable

import org.nikok.hextant.Editable
import org.nikok.hextant.core.expr.edited.IntLiteral
import org.nikok.reaktive.value.*

class EditableIntLiteral() : Editable<IntLiteral> {
    constructor(v: Int) : this() {
        text.set(v.toString())
    }

    val text = reactiveVariable("Text", "")

    override val edited: ReactiveValue<IntLiteral?> =
            text.map("Text converted to int") { it.toIntOrNull()?.let(::IntLiteral) }

    override val isOk: ReactiveBoolean
        get() = edited.map("$this is isOk") { it != null }
}