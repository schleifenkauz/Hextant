/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.core.editor.ListEditor
import hextant.project.ProjectItem
import hextant.project.view.EditorPane
import kotlinx.serialization.Transient
import reaktive.Observer

class ProjectItemListEditor<T> : ListEditor<ProjectItem<T>?, ProjectItemEditor<T, *>>() {
    @Transient
    private var commitChangeObserver: Observer? = null
    @Transient
    private var abortChangeObserver: Observer? = null

    override fun createEditor(): ProjectItemEditor<T, *> = FileEditor.newInstance(context)

    override fun editorRemoved(editor: ProjectItemEditor<T, *>, index: Int) {
        if (editor is FileEditor) {
            val root = editor.rootEditor
            context[EditorPane].deleted(root)
        }
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