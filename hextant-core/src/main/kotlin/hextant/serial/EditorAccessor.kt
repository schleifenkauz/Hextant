/**
 *@author Nikolaus Knop
 */

package hextant.serial

/**
 * An [EditorAccessor] connects a [hextant.core.Editor] with one of its children
 */
sealed class EditorAccessor

/**
 * The editor accessed by this accessor is a named component of another editor.
 *
 * @property propertyName the name of the property that references the accessed editor from its parent.
 */
data class PropertyAccessor(val propertyName: String) : EditorAccessor() {
    override fun toString(): String = ".$propertyName"
}

/**
 * The editor accessed by this accessor is a child of a [hextant.core.editor.ListEditor].
 *
 * @property index the zero-based index of this editor in the list of its siblings.
 */
data class IndexAccessor(val index: Int) : EditorAccessor() {
    override fun toString(): String = "[$index]"
}

/**
 * The editor accessed by this accessor is the child of an [hextant.core.editor.Expander].
 */
object ExpanderContent : EditorAccessor() {
    override fun toString(): String = ".editor.now"
}

/**
 * The editor accessed by this accessor is the child of an [hextant.core.editor.ChoiceEditor]
 * */
object ChoiceEditorContent : EditorAccessor() {
    override fun toString(): String = ".content.now"
}

/**
 * The editor accessed by this accessor is the child of an [hextant.core.editor.ChoiceEditor]
 * */
object OptionalEditorContent : EditorAccessor() {
    override fun toString(): String = ".optionalContent.now"
}
