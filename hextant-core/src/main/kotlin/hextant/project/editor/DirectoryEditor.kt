/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.*
import hextant.base.CompoundEditor
import hextant.project.Directory
import reaktive.value.now

class DirectoryEditor<R : Any>(
    context: Context
) : CompoundEditor<Directory<R>>(context), ProjectItemEditor<R, Directory<R>> {
    override val itemName by child(FileNameEditor(context))
    val items by child(ProjectItemListEditor<R>(context))

    fun isTaken(name: String, editor: FileNameEditor) = items.editors.now.any {
        val ed = it.itemName
        ed !== editor && ed.text.now == name
    }

    override val result: EditorResult<Directory<R>> =
        result2(itemName, items) { name, items -> ok(Directory(name, items)) }

    override fun deletePhysical() {}
}