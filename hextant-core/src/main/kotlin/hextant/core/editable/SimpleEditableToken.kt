/**
 *@author Nikolaus Knop
 */

package hextant.core.editable

open class SimpleEditableToken<out T : Any>(private val type: TokenType<T>) : EditableToken<T>(), TokenType<T> by type