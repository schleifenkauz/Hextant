/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.context.Context
import hextant.core.editor.ValidatedTokenEditor
import validated.*

/**
 * An editor for file names.
 */
class FileNameEditor(context: Context, text: String) : ValidatedTokenEditor<String>(context, text) {
    constructor(context: Context) : this(context, "")

    private fun parentDirectory(): DirectoryEditor<*>? {
        val item = parent as? ProjectItemEditor<*, *> ?: return null
        val list = item.parent as? ProjectItemListEditor<*> ?: return null
        return list.parent as? DirectoryEditor<*>
    }

    override fun compile(token: String): Validated<String> = when {
        token.isBlank()                                 -> invalid("Blank file names are invalid")
        token.any { it in forbiddenCharacters }         -> invalid("Invalid file name '$token'")
        parentDirectory()?.isTaken(token, this) == true -> invalid("Name '$token' already taken")
        else                                            -> valid(token)
    }

    companion object {
        private val forbiddenCharacters = "/\\".toSet()
    }
}