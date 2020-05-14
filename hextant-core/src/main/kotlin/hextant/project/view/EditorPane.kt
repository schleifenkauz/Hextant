/**
 *@author Nikolaus Knop
 */

package hextant.project.view

import bundles.SimpleProperty
import hextant.Editor

/**
 * An editor pane displays editors.
 */
interface EditorPane {
    /**
     * Create a view for the given [editor] and show it on the screen.
     */
    fun show(editor: Editor<*>)

    /**
     * If the given [editor] is currently shown by this [EditorPane] it must not be shown afterwards.
     */
    fun deleted(editor: Editor<*>)

    companion object : SimpleProperty<EditorPane>("editor pane")
}