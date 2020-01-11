/**
 *@author Nikolaus Knop
 */

package hextant.project.view

import hextant.Editor
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property

interface EditorPane {
    /**
     * Create a view for the given [editor] and show it on the screen.
     */
    fun show(editor: Editor<*>)

    /**
     * If the given [editor] is currently shown by this [EditorPane] it must not be shown afterwards.
     */
    fun deleted(editor: Editor<*>)

    companion object : Property<EditorPane, Public, Public>("editor pane")
}