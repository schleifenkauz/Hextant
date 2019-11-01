/**
 *@author Nikolaus Knop
 */

package hextant.blocky.view

import hextant.blocky.editor.EntryEditor
import hextant.bundle.Bundle
import javafx.scene.control.Label

class EntryEditorControl(editor: EntryEditor, args: Bundle) :
    ExecutableEditorControl<Label>(editor, args) {
    init {
        configureArrowStart(editor.next)
        styleClass.add("entry")
    }

    override fun createDefaultRoot(): Label = Label("entry").apply {
        styleClass.add("hextant-text")
    }
}