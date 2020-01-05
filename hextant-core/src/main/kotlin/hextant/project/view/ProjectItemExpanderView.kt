/**
 *@author Nikolaus Knop
 */

package hextant.project.view

import hextant.Editor
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.core.view.FXExpanderView
import hextant.core.view.TokenEditorControl
import hextant.get
import hextant.project.editor.*
import reaktive.event.Subscription

class ProjectItemExpanderView(private val expander: ProjectItemExpander<*>, args: Bundle) :
    FXExpanderView(expander, args) {
    private lateinit var commitChangeSubscription: Subscription
    private lateinit var abortChangeSubscription: Subscription

    override fun onExpansion(editor: Editor<*>, control: EditorControl<*>) {
        if (editor is DirectoryEditor<*> && control is DirectoryEditorControl) {
            val name = control.directoryName as? TokenEditorControl ?: return
            initialize(name)
        } else if (editor is FileEditor<*> && control is FileEditorControl) {
            val name = control.fileName as? TokenEditorControl ?: return
            initialize(name)
            commitChangeSubscription = name.commited.subscribe { _, _ ->
                val pane = context[EditorPane]
                pane.show(editor.root.get())
                commitChangeSubscription.cancel()
                abortChangeSubscription.cancel()
            }
            abortChangeSubscription = name.aborted.subscribe { _, _ ->
                expander.reset()
                abortChangeSubscription.cancel()
                commitChangeSubscription.cancel()
            }
        }
    }

    private fun initialize(name: TokenEditorControl) {
        name.editor.setText("_")
        name.root.text = ""
        name.beginChange()
    }
}