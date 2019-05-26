/**
 * @author Nikolaus Knop
 */

package hextant

import hextant.bundle.Bundle

/**
 * A group of [EditorView]'s of a common base type [V] that display a hierarchy of editors
 */
interface ViewGroup<V : EditorView> {
    /**
     * **Queries** the editor view that displays the given [editor] in this [ViewGroup]
     * @throws NoSuchElementException if [editor] is not displayed by this [ViewGroup]
     */
    fun getViewOf(editor: Editor<*>): V

    /**
     * **Creates** an editor view for the given editor
     */
    fun createViewFor(editor: Editor<*>, context: Context, arguments: Bundle): V
}