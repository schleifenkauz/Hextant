/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.core.editor.composeResult
import hextant.project.Directory
import reaktive.value.now
import validated.reaktive.ReactiveValidated

/**
 * An editor for directories.
 */
class DirectoryEditor<R>(
    context: Context
) : CompoundEditor<Directory<R>>(context), ProjectItemEditor<R, Directory<R>> {
    override val itemName by child(FileNameEditor(context))
    internal val items by child(ProjectItemListEditor<R>(context))

    internal fun isTaken(name: String, editor: FileNameEditor) = items.editors.now.any {
        val ed = it.itemName
        ed !== editor && ed.text.now == name
    }

    override fun supportsCopyPaste(): Boolean = true

    override val result: ReactiveValidated<Directory<R>> = composeResult(itemName, items)

    override fun deletePhysical() {}
}