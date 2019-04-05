/**
 *@author Nikolaus Knop
 */

package hextant.core.editable

import hextant.CompileResult

/**
 * A token type
 */
interface TokenType<out T> {
    /**
     * Compile the specified [tok] to a real token object
     */
    fun compile(tok: String): CompileResult<T>
}