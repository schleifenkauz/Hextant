/**
 *@author Nikolaus Knop
 */

package hextant.launcher.editor

import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.launcher.Files
import hextant.launcher.Files.Companion.PROJECTS
import java.io.File

internal open class ProjectNameEditor private constructor(val isCreate: Boolean, context: Context) :
    TokenEditor<File?, TokenEditorView>(context) {
    override fun compile(token: String): File? =
        token.takeIf { it.matches(REGEX) }?.let { context[Files].getProject(token) }

    class Create(context: Context) : ProjectNameEditor(true, context)
    class Reference(context: Context) : ProjectNameEditor(false, context)

    companion object {
        private val REGEX = Regex("[^/\\\\:; \t\r\n]+")
    }
}