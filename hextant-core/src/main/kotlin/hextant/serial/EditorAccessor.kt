/**
 *@author Nikolaus Knop
 */

package hextant.serial

/**
 * An [EditorAccessor] connects a [hextant.Editor] with one of its children
 */
sealed class EditorAccessor

internal data class PropertyAccessor(val propertyName: String) : EditorAccessor() {
    override fun toString(): String = ".$propertyName"
}

internal data class IndexAccessor(val index: Int) : EditorAccessor() {
    override fun toString(): String = "[$index]"
}

internal object ExpanderContent : EditorAccessor() {
    override fun toString(): String = ".editor.now"
}