/**
 *@author Nikolaus Knop
 */

package hextant.project.view

import hextant.Editor
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.core.editor.TokenEditor
import hextant.core.view.FXExpanderView
import hextant.core.view.TokenEditorControl
import hextant.get
import hextant.project.editor.*
import reaktive.event.Subscription
import reaktive.value.now

class ProjectItemExpanderView(private val expander: ProjectItemExpander<*>, args: Bundle) :
    FXExpanderView(expander, args) {
    private lateinit var subscription: Subscription

    override fun onExpansion(editor: Editor<*>, control: EditorControl<*>) {
        if (editor is DirectoryEditor<*> && control is DirectoryEditorControl) {
            val name = control.directoryName as? TokenEditorControl ?: return
            expandedTo(editor, editor.directoryName, name)
        } else if (editor is FileEditor<*> && control is FileEditorControl) {
            val name = control.fileName as? TokenEditorControl ?: return
            expandedTo(editor, editor.name, name)
            subscription = name.endedChange.subscribe { _, _ ->
                val pane = context[EditorPane]
                pane.show(editor.root.get())
                subscription.cancel()
            }
        }
    }

    private fun expandedTo(editor: ProjectItemEditor<*, *>, name: TokenEditor<*, *>, nameControl: TokenEditorControl) {
        val parent = expander.parent.now?.parent?.now as? DirectoryEditor<*> ?: return
        val suggested = parent.suggestName(editor)
        nameControl.root.text = suggested
        nameControl.root.selectAll()
        name.setText(suggested)
        nameControl.beginChange()

    }
}