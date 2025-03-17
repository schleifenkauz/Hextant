/**
 *@author Nikolaus Knop
 */

package hextant.expr

import hextant.core.Editor
import hextant.expr.editor.ExpressionEditor
import hextant.project.ProjectType
import hextant.test.showTestProject

object CommandsDemo : ProjectType {
    override fun createProject(): Editor<*> = ExpressionEditor()

    @JvmStatic
    fun main(args: Array<String>) {
        showTestProject(this)
    }
}