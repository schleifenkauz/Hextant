/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant

/**
 * An Editor which acts like a Controller in the MVC pattern
*/
interface Editor<E: Editable<*>> {
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
     * @return the parent of this editor, typically this is the editor of the parent of editable of this editor
    */
    val parent: Editor<*>? get() = null

    /**
     * @return the children of this editor, typically this are the editors of the children of the editable of this editor
     */
    val children: Collection<Editor<*>>? get() = null
}