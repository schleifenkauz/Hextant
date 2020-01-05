/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.*
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import reaktive.value.now

class FileNameEditor(context: Context, text: String) : TokenEditor<String, TokenEditorView>(context, text) {
    constructor(context: Context) : this(context, "")

    private fun parentDirectory(): DirectoryEditor<*>? {
        val item = parent.now as? ProjectItemEditor<*, *> ?: return null
        val list = item.parent.now as? ProjectItemListEditor<*> ?: return null
        return list.parent.now as? DirectoryEditor<*>
    }

    override fun compile(token: String): CompileResult<String> = when {
        token.isBlank()                                 -> err("Blank file names are invalid")
        token.any { it in forbiddenCharacters }         -> err("Invalid file name '$token'")
        parentDirectory()?.isTaken(token, this) == true -> err("Name '$token' already taken")
        else                                            -> ok(token)
    }

    companion object {
        private val forbiddenCharacters = "/\\".toSet()
    }
}