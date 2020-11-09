/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.context.Context
import hextant.core.editor.NullableListEditor
import hextant.project.ProjectItem
import hextant.project.view.EditorPane
import reaktive.Observer

internal class ProjectItemListEditor<T>(context: Context) :
    NullableListEditor<ProjectItem<T>?, ProjectItemEditor<T, *>>(context) {
    private var commitChangeObserver: Observer? = null
    private var abortChangeObserver: Observer? = null

    override fun createEditor(): ProjectItemEditor<T, *> = FileEditor.newInstance(context)

    override fun editorRemoved(editor: ProjectItemEditor<T, *>, index: Int) {
        if (editor is FileEditor) {
            val root = editor.rootEditor
            context[EditorPane].deleted(root)
        }
        editor.deletePhysical()
    }

    override fun editorAdded(editor: ProjectItemEditor<T, *>, index: Int) {
        val name = editor.itemName
        name.recompile()
        commitChangeObserver = name.commitedChange.observe { _, _ ->
            if (editor is FileEditor<*>) {
                val pane = context[EditorPane]
                pane.show(editor.rootEditor)
            }
            killObservers()
        }
        abortChangeObserver = name.abortedChange.observe { _, _ ->
            remove(editor)
            killObservers()
        }
    }

    private fun killObservers() {
        abortChangeObserver?.kill()
        commitChangeObserver?.kill()
        abortChangeObserver = null
        commitChangeObserver = null
    }
}