/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.*
import hextant.base.CompoundEditor
import hextant.project.Directory
import hextant.serial.*
import reaktive.value.now

class DirectoryEditor<R : Any>(
    context: Context,
    name: FileNameEditor = FileNameEditor(context),
    content: ProjectItemListEditor<R> = ProjectItemListEditor(context)
) : CompoundEditor<Directory<R>>(context), ProjectItemEditor<R, Directory<R>> {
    val directoryName by child(name, context)
    val items by child(content, context)

    override val path: ReactivePath by lazy {
        val parentPath = getProjectItemEditorParent()?.path ?: ReactivePath.empty()
        parentPath.resolve(directoryName.text)
    }

    private fun nameEditor(item: ProjectItemEditor<*, *>): FileNameEditor? = when (item) {
        is FileEditor          -> item.fileName
        is DirectoryEditor     -> item.directoryName
        is ProjectItemExpander -> item.editor.now?.let { nameEditor(it) }
        else                   -> null
    }

    fun isTaken(name: String, editor: FileNameEditor) = items.editors.now.any {
        val ed = nameEditor(it)
        ed != null && ed !== editor && ed.text.now == name
    }

    override val result: EditorResult<Directory<R>> =
        result2(directoryName, items) { name, items -> ok(Directory(name, items)) }

    override fun deletePhysical() {
        context[HextantFileManager].deleteDirectory(path)
    }
}