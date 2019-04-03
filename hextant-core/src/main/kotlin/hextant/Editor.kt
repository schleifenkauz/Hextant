/**
 * @author Nikolaus Knop
 */

package hextant

import hextant.base.ParentEditor
import hextant.core.editor.Expander

/**
 * An Editor which acts like a Controller in the MVC pattern
 */
interface Editor<out E : Editable<*>> {
    /**
     * @return the [Editable] edited by this [Editor]
     */
    val editable: E

    /**
     * Select this editor
     * There are three cases
     * 1. No editor was selected previously -> This editor will be the only selected editor
     * 2. Only this editor was selected -> Only this editor will remain selected
     * 3. One or more other editors and possibly this editor were selected -> Only this editor will be selected
     */
    fun select()

    /**
     * Toggle selection of this editor
     * There are four cases
     * 1. Only this editor was selected previously -> This editor will remain selected
     * 2. No editor was selected -> This will be the only selected editor
     * 3. This editor and one or more other editors were selected -> Only this editor will become unselected
     * 4. This editor wasn't selected and one ore more other editors were selected ->
     * The previously selected editors and this editor will be selected
     */
    fun toggleSelection()

    /**
     * Add selection for this [Editor] and deselect the [child]
     * * Calls of [shrinkSelection] will focus the specified [child]
     */
    fun extendSelection(child: Editor<*>)

    /**
     * Focus the last child that expanded selection on this editor
     */
    fun shrinkSelection()

    /**
     * @return whether this editor is selected currently
     */
    val isSelected: Boolean

    /**
     * The parent of this editor or `null` if it has no parent
     */
    val parent: ParentEditor<*, *>?

    /**
     * The expander that expanded this [Editor] or `if` this editor wasn't expanded
     */
    val expander: Expander<*, *>?

    /**
     * The children of this editor
     *
     * Default implementation returns an empty list
     */
    val children: Collection<Editor<*>> get() = emptyList()

    /**
     * Move this [Editor] to its specified [newParent] by
     * * Setting the newParent of this [Editor] to [newParent]
     * * And adding this [Editor] to the children of [newParent]
     * * If the specified parent is an [Expander] then instead of the above operations the expander is set and
     * the parent is set to the parent of the expander
     * If this [Editor] is already a child of [newParent] this method has no effect
     * @throws IllegalArgumentException if [newParent] doesn't accept this [Editor] as a child
     */
    fun moveTo(newParent: ParentEditor<*, *>?)

    /**
     * @return all recursive children of this editor
     */
    val allChildren: Sequence<Editor<*>> get() = emptySequence()
}