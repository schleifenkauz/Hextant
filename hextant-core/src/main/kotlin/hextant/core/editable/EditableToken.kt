/**
 *@author Nikolaus Knop
 */

package hextant.core.editable

import hextant.base.AbstractEditable
import kserial.*
import reaktive.value.binding.map
import reaktive.value.reactiveVariable

/**
 * An editable token the atomic part of the ast editor
 * * In a view it would be typically represented by a text field
 * @param T the type of the token being compiled
 */
abstract class EditableToken<out T : Any> : AbstractEditable<T>(), Serializable, TokenType<T> {
    /**
     * The uncompiled text
     * * When setting it the [result] token is recompiled
     */
    val text = reactiveVariable("")

    override val result = text.map { t -> compile(t) }

    override fun deserialize(input: Input, context: SerialContext) {
        text.set(input.readString())
    }

    override fun serialize(output: Output, context: SerialContext) {
        output.writeString(text.get())
    }
}