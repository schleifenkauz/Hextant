/**
 *@author Nikolaus Knop
 */

package hextant.launcher.editor

import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.launcher.Files
import hextant.launcher.ProjectManager
import validated.*

internal class ProjectLocationEditor constructor(context: Context) : TokenEditor<String, TokenEditorView>(context) {
    override fun compile(token: String): String {
        val f = context[Files].getProject(token)
        return when {
            !f.exists() || !f.resolve(Files.PROJECT_INFO).exists() ->
                invalid("Project with name '$token' doesn't exist")
            context[ProjectManager].isLocked(f)                    ->
                invalid("Project '$token' already opened by another editor")
            else                                                   -> valid(token)
        }
    }

    override fun compile(item: Any): Validated<String> = when (item) {
        is String -> valid(item)
        else      -> invalidComponent
    }
}