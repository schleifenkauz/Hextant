/**
 *@author Nikolaus Knop
 */

package hextant.expr

import hextant.context.Context
import hextant.core.Editor
import hextant.expr.editor.ExpressionEditor
import hextant.project.ProjectType
import hextant.test.HextantTestApplication

class CommandsDemo : HextantTestApplication(CommandsDemo) {
    companion object : ProjectType {
        override fun createProject(context: Context): Editor<*> = ExpressionEditor(context)

        @JvmStatic
        fun main(args: Array<String>) {
            launch<CommandsDemo>()
        }
    }
}