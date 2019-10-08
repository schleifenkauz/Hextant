/**
 *@author Nikolaus Knop
 */

package hextant.blocky

import hextant.*
import hextant.blocky.editor.ProgramEditor
import hextant.main.HextantApplication
import hextant.undo.UndoManager
import javafx.scene.Parent

class BlockyApp : HextantApplication() {
    override fun createContext(platform: HextantPlatform): Context = Context.newInstance(platform) {
        set(UndoManager, UndoManager.newInstance())
    }

    override fun createView(context: Context): Parent {
        val e = ProgramEditor(context)
        return context.createView(e)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(BlockyApp::class.java)
        }
    }
}