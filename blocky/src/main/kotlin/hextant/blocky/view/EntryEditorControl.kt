/**
 *@author Nikolaus Knop
 */

package hextant.blocky.view

import bundles.Bundle
import hextant.blocky.editor.EntryEditor
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import javafx.scene.control.Label

class EntryEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    editor: EntryEditor,
    argumnts: Bundle
) :
    ExecutableEditorControl<Label>(editor, argumnts) {
    init {
        configureArrowStart(editor.next)
        styleClass.add("entry")
    }

    override fun createDefaultRoot(): Label = Label("entry").apply {
        styleClass.add("hextant-text")
    }
}