/**
 *@author Nikolaus Knop
 */

package hextant.project.view

import hextant.Editor
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property

interface EditorPane {
    fun show(editor: Editor<*>)

    fun deleted(editor: Editor<*>)

    companion object : Property<EditorPane, Public, Public>("editor pane")
}