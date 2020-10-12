/**
 *@author Nikolaus Knop
 */

package hextant.expr

import hextant.context.Context
import hextant.core.Editor
import hextant.expr.editor.ExprExpander
import hextant.project.ProjectType
import hextant.test.HextantTestApplication

class ExpanderDemo : HextantTestApplication(ExpanderDemo) {
    companion object : ProjectType {
        override fun createProject(context: Context): Editor<*> = ExprExpander(context)

        @JvmStatic
        fun main(args: Array<String>) {
            launch<ExpanderDemo>()
        }
    }
}