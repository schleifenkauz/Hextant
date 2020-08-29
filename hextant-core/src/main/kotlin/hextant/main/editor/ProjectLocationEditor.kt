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
import java.io.File

internal class ProjectLocationEditor constructor(context: Context) : TokenEditor<File, TokenEditorView>(context) {
    private fun getFile(projectName: String) = context[GlobalDirectory][PROJECTS].resolve(projectName)

    override fun compile(token: String): Validated<File> {
        val f = getFile(token)
        return if (!f.exists() || !f.resolve(GlobalDirectory.PROJECT_INFO).exists())
            invalid("Project with name '$token' doesn't exist")
        else valid(f)
    }

    override fun compile(item: Any): Validated<File> = when (item) {
        is String -> valid(getFile(item))
        else      -> invalidComponent
    }
}