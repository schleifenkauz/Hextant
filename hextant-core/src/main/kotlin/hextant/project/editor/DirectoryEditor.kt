/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.Context
import hextant.base.CompoundEditor
import hextant.core.editor.composeResult
import hextant.project.Directory
import reaktive.value.now
import validated.reaktive.ReactiveValidated

class DirectoryEditor<R : Any>(
    context: Context
) : CompoundEditor<Directory<R>>(context), ProjectItemEditor<R, Directory<R>> {
    override val itemName by child(FileNameEditor(context))
    val items by child(ProjectItemListEditor<R>(context))

    fun isTaken(name: String, editor: FileNameEditor) = items.editors.now.any {
        val ed = it.itemName
        ed !== editor && ed.text.now == name
    }

    override val result: ReactiveValidated<Directory<R>> = composeResult(itemName, items)

    override fun deletePhysical() {}
}