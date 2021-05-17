/**
 *@author Nikolaus Knop
 */

package hextant.launcher.editor

import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.cli.HextantDirectory
import java.io.File

internal open class ProjectNameEditor(val isCreate: Boolean, context: Context) :
    TokenEditor<File?, TokenEditorView>(context) {
    override fun compile(token: String): File? =
        token.takeIf { it.matches(REGEX) }?.let { HextantDirectory.getProject(token) }

    companion object {
        private val REGEX = Regex("[^/\\\\:; \t\r\n]+")
    }
}