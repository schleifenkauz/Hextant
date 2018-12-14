/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.editable

import kserial.*
import org.nikok.hextant.Editable
import org.nikok.reaktive.value.*

/**
 * An editable token is the atomic part of the ast editor
 * * In a view it would be typically represented by a text field
 * @param T the type of the token being compiled
 */
abstract class EditableToken<out T : Any> : Editable<T>, Serializable {
    /**
     * @return whether the passed [tok] is valid or not
     */
    protected abstract fun isValid(tok: String): Boolean

    /**
     * Compile the specified [tok] to a real token object
     * * Is only called when `isValid(tok)` returned `true` after the last invalidation of [text]
     */
    protected abstract fun compile(tok: String): T

    /**
     * The uncompiled text
     * * When setting it the [edited] token is recompiled
     */
    val text = reactiveVariable("text", "")

    override val isOk: ReactiveBoolean = text.map("is ok") { t -> isValid(t) }

    override val edited: ReactiveValue<T?> = text.map("compiled text") { t ->
        if (isOk.now) compile(t)
        else null
    }

    override fun deserialize(input: Input, context: SerialContext) {
        text.set(input.readString())
    }

    override fun serialize(output: Output, context: SerialContext) {
        output.writeString(text.get())
    }

    companion object {
        /**
         * @return an [EditableToken] which uses the specified [regex] to validate the tokens
         */
        fun <T : Any> withRegex(regex: Regex, compile: (String) -> T): EditableToken<T> = object : EditableToken<T>() {
            override fun isValid(tok: String): Boolean = tok matches regex

            override fun compile(tok: String): T = compile.invoke(tok)
        }

        fun withRegex(regex: Regex): EditableToken<String> =
            withRegex(regex) { it }

        fun checking(pred: (String) -> Boolean) = object : EditableToken<String>() {
            override fun isValid(tok: String): Boolean = pred(tok)

            override fun compile(tok: String): String = tok
        }
    }
}