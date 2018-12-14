/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editable

import kserial.*
import org.nikok.hextant.Editable
import org.nikok.reaktive.value.*

class EditableText(override val parent: Editable<*>? = null) : Editable<String>, Serializable {
    val text = reactiveVariable("text of $this", "")

    override val edited: ReactiveVariable<String> get() = text

    override val isOk: ReactiveBoolean = reactiveValue("is $this ok", true)

    override fun deserialize(input: Input, context: SerialContext) {
        text.set(input.readString())
    }

    override fun serialize(output: Output, context: SerialContext) {
        output.writeString(text.get())
    }
}