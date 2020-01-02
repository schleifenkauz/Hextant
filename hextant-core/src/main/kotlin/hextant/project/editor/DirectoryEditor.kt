/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.*
import hextant.base.CompoundEditor
import hextant.project.Directory

class DirectoryEditor<R : Any>(
    context: Context,
    name: FileNameEditor = FileNameEditor(context),
    content: ProjectItemListEditor<R> = ProjectItemListEditor(context)
) : CompoundEditor<Directory<R>>(context), ProjectItemEditor<R, Directory<R>> {
    fun suggestName(editor: ProjectItemEditor<*, *>): String = when (editor) {
        is DirectoryEditor -> "New Directory"
        is FileEditor      -> "New File"
        else               -> "New Item"
    }

    val directoryName by child(name, context)
    val items by child(content, context)

    override val result: EditorResult<Directory<R>> =
        result2(directoryName, items) { name, items -> ok(Directory(name, items)) }
}