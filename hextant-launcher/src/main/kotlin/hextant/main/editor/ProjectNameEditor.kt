/**
 *@author Nikolaus Knop
 */

package hextant.main.editor

import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.main.GlobalDirectory
import hextant.main.GlobalDirectory.Companion.PROJECTS
import validated.*

internal class ProjectNameEditor(context: Context) : TokenEditor<String, TokenEditorView>(context) {
    override fun compile(token: String): Validated<String> = when {
        !token.matches(REGEX)                                         -> invalid("Invalid project name '$token'")
        context[GlobalDirectory][PROJECTS].resolve(token).isDirectory -> invalid("Project with name '$token' already exists")
        else                                                          -> valid(token)
    }

    companion object {
        private val REGEX = Regex("[^/\\\\:; \t\r\n]+")
    }
}