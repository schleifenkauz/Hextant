/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.context.Context
import hextant.core.editor.ValidatedTokenEditor

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

    override fun wrap(token: String): String? = when {
        token.isBlank()                                 -> null
        token.any { it in forbiddenCharacters }         -> null
        parentDirectory()?.isTaken(token, this) == true -> null
        else                                            -> token
    }

    companion object {
        private val forbiddenCharacters = "/\\".toSet()
    }
}