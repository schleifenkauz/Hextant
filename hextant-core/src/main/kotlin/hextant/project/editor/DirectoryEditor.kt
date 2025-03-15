/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.core.editor.CompoundEditor
import hextant.project.Directory
import reaktive.value.ReactiveValue
import reaktive.value.binding.binding
import reaktive.value.now

/**
 * An editor for directories.
 */
class DirectoryEditor<R> : CompoundEditor<Directory<R>?>(), ProjectItemEditor<R, Directory<R>> {
    override val itemName by child(FileNameEditor())
    internal val items by child(ProjectItemListEditor<R>())

    internal fun isTaken(name: String, editor: FileNameEditor) = items.editors.now.any {
        val ed = it.itemName
        ed !== editor && ed.text.now == name
    }

    override fun supportsCopyPaste(): Boolean = true

    override val result: ReactiveValue<Directory<R>?> = binding(itemName.result, items.result) { name, children ->
        Directory(name, children.filterNotNull())
    }
}