/**
 *@author Nikolaus Knop
 */

package hextant.launcher.editor

import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.launcher.Files
import hextant.launcher.Files.Companion.PROJECTS

internal class ProjectNameEditor(context: Context) : TokenEditor<String, TokenEditorView>(context) {
    override fun wrap(token: String): String? = when {
        !token.matches(REGEX)                               -> null
        context[Files][PROJECTS].resolve(token).isDirectory -> null
        else                                                -> token
    }

    companion object {
        private val REGEX = Regex("[^/\\\\:; \t\r\n]+")
    }
}