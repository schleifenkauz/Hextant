/**
 *@author Nikolaus Knop
 */

package hextant.expr

import hextant.context.Context
import hextant.core.Editor
import hextant.expr.editor.OperatorApplicationEditor
import hextant.project.ProjectType
import hextant.test.showTestProject

object CompoundEditorDemo : ProjectType {
    override fun createProject(context: Context): Editor<*> = OperatorApplicationEditor(context)

    @JvmStatic
    fun main(args: Array<String>) {
        showTestProject(this)
    }
}
