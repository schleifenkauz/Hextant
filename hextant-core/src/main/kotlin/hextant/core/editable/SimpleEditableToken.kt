/**
 *@author Nikolaus Knop
 */

package hextant.core.editable

class SimpleEditableToken<out T : Any>(type: TokenType<T>) : EditableToken<T>(), TokenType<T> by type