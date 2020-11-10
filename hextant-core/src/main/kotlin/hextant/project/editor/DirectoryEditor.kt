/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.project.Directory
import reaktive.value.ReactiveValue
import reaktive.value.now

/**
 * An editor for directories.
 */
class DirectoryEditor<R : Any>(
    context: Context
) : CompoundEditor<Directory<R>>(context), ProjectItemEditor<R, Directory<R>> {
    override val itemName by child(FileNameEditor(context))
    internal val items by child(ProjectItemListEditor<R>(context))

    internal fun isTaken(name: String, editor: FileNameEditor) = items.editors.now.any {
        val ed = it.itemName
        ed !== editor && ed.text.now == name
    }

    override val result: ReactiveValue<Directory<R>?> = composeResult { Directory(itemName.get(), items.get()) }

    override fun supportsCopyPaste(): Boolean = true

    override fun deletePhysical() {}
}