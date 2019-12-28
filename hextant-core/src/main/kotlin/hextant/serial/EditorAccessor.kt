/**
 *@author Nikolaus Knop
 */

package hextant.serial

/**
 * An [EditorAccessor] connects a [hextant.Editor] with one of its children
 */
sealed class EditorAccessor

data class PropertyAccessor(val propertyName: String) : EditorAccessor() {
    override fun toString(): String = ".$propertyName"
}

data class IndexAccessor(val index: Int) : EditorAccessor() {
    override fun toString(): String = "[$index]"
}