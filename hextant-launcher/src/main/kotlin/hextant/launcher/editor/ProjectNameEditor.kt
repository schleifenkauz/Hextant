/**
 *@author Nikolaus Knop
 */

package hextant.launcher.editor

import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.serial.Files
import java.io.File

internal open class ProjectNameEditor(val isCreate: Boolean, context: Context) :
    TokenEditor<File?, TokenEditorView>(context) {
    override fun compile(token: String): File? =
        token.takeIf { it.matches(REGEX) }?.let { context[Files].getProject(token) }

    companion object {
        private val REGEX = Regex("[^/\\\\:; \t\r\n]+")
    }
}