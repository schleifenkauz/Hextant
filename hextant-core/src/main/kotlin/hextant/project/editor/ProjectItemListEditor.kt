/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.Context
import hextant.core.editor.ListEditor
import hextant.get
import hextant.project.ProjectItem
import hextant.project.view.EditorPane
import reaktive.value.now

class ProjectItemListEditor<T : Any>(context: Context) :
    ListEditor<ProjectItem<T>, ProjectItemEditor<T, *>>(context) {
    override fun createEditor(): ProjectItemEditor<T, *> = ProjectItemExpander(context)

    override fun editorRemoved(editor: ProjectItemEditor<T, *>, index: Int) {
        val p = editor.path ?: return
        if (editor is ProjectItemExpander && editor.editor.now is FileEditor<*>) {
            val file = editor.editor.now as FileEditor<*>
            val root = file.rootEditor
            context[EditorPane].deleted(root)
        }
        editor.deletePhysical()
    }
}