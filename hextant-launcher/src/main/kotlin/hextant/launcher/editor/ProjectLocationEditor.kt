/**
 *@author Nikolaus Knop
 */

package hextant.launcher.editor

import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.launcher.Files
import hextant.launcher.ProjectManager

internal class ProjectLocationEditor constructor(context: Context) : TokenEditor<String?, TokenEditorView>(context) {
    override fun compile(token: String): String? {
        val f = context[Files].getProject(token)
        return when {
            !f.exists() || !f.resolve(Files.PROJECT_INFO).exists() -> null
            context[ProjectManager].isLocked(f)                    -> null
            else                                                   -> token
        }
    }
}