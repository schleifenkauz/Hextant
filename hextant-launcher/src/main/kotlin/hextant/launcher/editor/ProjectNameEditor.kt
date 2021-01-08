/**
 *@author Nikolaus Knop
 */

package hextant.launcher.editor

import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.launcher.Files
import hextant.launcher.Files.Companion.PROJECTS
import validated.invalid
import validated.valid

internal class ProjectNameEditor(context: Context) : TokenEditor<String, TokenEditorView>(context) {
    override fun compile(token: String): String = when {
        !token.matches(REGEX)                               -> invalid("Invalid project name '$token'")
        context[Files][PROJECTS].resolve(token).isDirectory -> invalid("Project with name '$token' already exists")
        else                                                -> valid(token)
    }

    companion object {
        private val REGEX = Regex("[^/\\\\:; \t\r\n]+")
    }
}