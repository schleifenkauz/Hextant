/**
 * @author Nikolaus Knop
 */

package hextant.expr

import hextant.context.Context
import hextant.core.Editor
import hextant.expr.editor.IntLiteralEditor
import hextant.project.ProjectType
import hextant.test.HextantTestApplication

class LiteralsDemo : HextantTestApplication(LiteralsDemo) {
    companion object : ProjectType {
        override fun createProject(context: Context): Editor<*> = IntLiteralEditor(context)

        @JvmStatic
        fun main(args: Array<String>) {
            launch<LiteralsDemo>()
        }
    }
}