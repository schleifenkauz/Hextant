/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

/**
 * A token type is able to compile text to results of type [R]
 */
fun interface TokenType<out R> {
    /**
     * Create a result from the given textual [token].
     */
    fun compile(token: String): R
}