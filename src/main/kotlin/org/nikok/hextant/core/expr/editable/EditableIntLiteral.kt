/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editable

import org.nikok.hextant.Editable
import org.nikok.hextant.core.expr.edited.IntLiteral
import org.nikok.reaktive.value.*

class EditableIntLiteral(override val parent: Editable<*>? = null) : Editable<IntLiteral> {
    constructor(v: Int, parent: Editable<*>? = null) : this(parent) {
        text.set(v.toString())
    }

    val text = reactiveVariable("Text", "")

    override val edited: ReactiveValue<IntLiteral?> =
            text.map("Text converted to int") { it.toIntOrNull()?.let(::IntLiteral) }

    override val isOk: ReactiveBoolean
        get() = edited.map("$this is isOk") { it != null }
}