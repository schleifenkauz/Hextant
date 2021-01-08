/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

import validated.Validated

/**
 * A token type is able to compile text to results of type [R]
 */
interface TokenType<out R> {
    /**
     * Compile the given [token].
     *
     * If the [token] is a valid one return an instance of [Validated.Valid] wrapping the compiled result.
     * Otherwise return an instance of [Validated.Invalid] with a specific error message.
     */
    fun compile(token: String): R
}