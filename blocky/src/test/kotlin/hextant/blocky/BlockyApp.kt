/**
 *@author Nikolaus Knop
 */

package hextant.blocky

import hextant.blocky.editor.ProgramEditor
import hextant.context.Context
import hextant.context.createControl
import hextant.fx.registerShortcuts
import hextant.main.HextantApplication
import javafx.scene.Parent
import reaktive.value.now
import validated.ifInvalid

class BlockyApp : HextantApplication() {
    override fun createView(context: Context): Parent {
        val e = ProgramEditor(context)
        val view = context.createControl(e)
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