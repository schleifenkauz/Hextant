/**
 *@author Nikolaus Knop
 */

package hextant.main.editor

import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.main.GlobalDirectory
import validated.*

internal class ProjectLocationEditor constructor(context: Context) : TokenEditor<String, TokenEditorView>(context) {
    override fun compile(token: String): Validated<String> {
        val f = context[GlobalDirectory].getProject(token)
        return if (!f.exists() || !f.resolve(GlobalDirectory.PROJECT_INFO).exists())
            invalid("Project with name '$token' doesn't exist")
        else valid(token)
    }

    override fun compile(item: Any): Validated<String> = when (item) {
        is String -> valid(item)
        else      -> invalidComponent
    }
}