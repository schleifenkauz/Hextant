/**
 * @author Nikolaus Knop
 */

package hextant.sample

import hextant.context.Context
import hextant.context.createView
import hextant.main.HextantApplication
import hextant.sample.ast.editor.AltExpander
import javafx.scene.Parent

class SampleApp : HextantApplication() {
    override fun createView(context: Context): Parent {
        val editor = AltExpander(context)
        return context.createView(editor)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(SampleApp::class.java)
        }
    }
}