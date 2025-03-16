/**
 *@author Nikolaus Knop
 */

package hextant.expr

import hextant.core.Editor
import hextant.expr.editor.ExprExpander
import hextant.project.ProjectType
import hextant.test.showTestProject

object ExpanderDemo : ProjectType {
    override fun createProject(): Editor<*> = ExprExpander()

    @JvmStatic
    fun main(args: Array<String>) {
        showTestProject(this)
    }
}
