/**
 * @author Nikolaus Knop
 */

package hextant.core

import hextant.CompileResult

/**
 * A token type is able to compile text to results of type [R]
 */
interface TokenType<out R> {
    /**
     * Compile the given [token]
     */
    fun compile(token: String): CompileResult<R>
}