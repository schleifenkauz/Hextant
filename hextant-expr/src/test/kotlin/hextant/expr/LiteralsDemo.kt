/**
 * @author Nikolaus Knop
 */

package hextant.expr

import hextant.core.Editor
import hextant.expr.editor.IntLiteralEditor
import hextant.project.ProjectType
import hextant.test.showTestProject

object LiteralsDemo : ProjectType {
    override fun createProject(): Editor<*> = IntLiteralEditor()

    @JvmStatic
    fun main(args: Array<String>) {
        showTestProject(this)
    }
}