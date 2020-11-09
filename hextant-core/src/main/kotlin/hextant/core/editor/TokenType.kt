/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

/**
 * A token type is able to transform text to results of type [R]
 */
interface TokenType<out R> {
    /**
     * Transform the given [token] to a result or return `null` if the [token] is not valid.
     */
    fun wrap(token: String): R
}