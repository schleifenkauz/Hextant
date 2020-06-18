/**
 * @author Nikolaus Knop
 */

package hextant.sample

import hextant.context.Context
import hextant.context.createView
import hextant.main.HextantApplication
import hextant.sample.ast.editor.AltExpander
import hextant.undo.UndoManager
import javafx.scene.Parent

class SampleApp : HextantApplication() {
    override fun createContext(root: Context): Context = Context.newInstance(root) {
        set(UndoManager, UndoManager.newInstance())
    }

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