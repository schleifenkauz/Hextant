/**
 * @author Nikolaus Knop
 */

package hextant.core

import hextant.CompileResult

interface TokenType<out R> {
    fun compile(token: String): CompileResult<R>
}