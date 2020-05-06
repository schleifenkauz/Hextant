/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.Context
import hextant.core.editor.ListEditor
import hextant.project.ProjectItem
import hextant.project.view.EditorPane
import hextant.serial.HextantFileManager
import hextant.serial.now
import reaktive.Observer

class ProjectItemListEditor<T : Any>(context: Context) :
    ListEditor<ProjectItem<T>, ProjectItemEditor<T, *>>(context) {
    private var commitChangeObserver: Observer? = null
    private var abortChangeObserver: Observer? = null
    private var renamer: Observer? = null

    override fun createEditor(): ProjectItemEditor<T, *> = FileEditor.newInstance(context)

    override fun editorRemoved(editor: ProjectItemEditor<T, *>, index: Int) {
        if (editor is FileEditor) {
            val root = editor.rootEditor
            context[EditorPane].deleted(root)
        }
        editor.deletePhysical()
    }

    override fun editorAdded(editor: ProjectItemEditor<T, *>, index: Int) {
        val name = editor.getItemNameEditor() ?: error("Unexpected project item editor")
        name.beginChange()
        name.recompile()
        commitChangeObserver = name.commitedChange.observe { _, _ ->
            renamer?.kill()
            renamer = editor.renamePhysicalOnNameChange()
            if (editor is FileEditor<*>) {
                editor.initialize()
                val pane = context[EditorPane]
                pane.show(editor.rootEditor)
            } else if (editor is DirectoryEditor<*>) {
                context[HextantFileManager].createDirectory(editor.path.now)
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