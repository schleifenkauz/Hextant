/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.Context
import hextant.core.editor.ListEditor
import hextant.project.ProjectItem

class ProjectItemListEditor<T : Any>(context: Context) :
    ListEditor<ProjectItem<T>, ProjectItemEditor<T, *>>(context) {
    override fun createEditor(): ProjectItemEditor<T, *> = ProjectItemExpander(context)
}