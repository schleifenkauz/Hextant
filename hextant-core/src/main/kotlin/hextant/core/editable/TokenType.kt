/**
 *@author Nikolaus Knop
 */

package hextant.core.editable

/**
 * A token type
 */
interface TokenType<out T> {
    /**
     * @return whether the passed [tok] is valid or not
     */
    fun isValid(tok: String): Boolean

    /**
     * Compile the specified [tok] to a real token object
     * * Is only called when `isValid(tok)` returned `true` after the last invalidation of [text]
     */
    fun compile(tok: String): T
}