/**
 * @author Nikolaus Knop
 */

package hextant.sample

import hextant.*
import hextant.main.HextantApplication
import hextant.sample.ast.editor.AltExpander
import hextant.undo.UndoManager
import hextant.undo.UndoManager.Companion
import javafx.scene.Parent

class SampleApp: HextantApplication() {
    override fun createContext(platform: HextantPlatform): Context = Context.newInstance(platform) {
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