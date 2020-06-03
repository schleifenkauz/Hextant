/**
 *@author Nikolaus Knop
 */

package hextant.blocky

import hextant.*
import hextant.blocky.editor.ProgramEditor
import hextant.fx.registerShortcuts
import hextant.main.HextantApplication
import javafx.scene.Parent
import reaktive.value.now
import validated.ifInvalid

class BlockyApp : HextantApplication() {
    override fun createContext(root: Context): Context = HextantPlatform.defaultContext(root)

    override fun createView(context: Context): Parent {
        val e = ProgramEditor(context)
        val view = context.createView(e)
        view.registerShortcuts {
            on("Ctrl+E") {
                val prog = e.result.now.ifInvalid { return@on }
                execute(prog)
            }
        }
        return view
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(BlockyApp::class.java)
        }
    }
}