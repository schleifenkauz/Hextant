/**
 *@author Nikolaus Knop
 */

package hackus.gui

import hackus.editable.EditableDefinitionList
import hextant.*
import hextant.Context.Companion.newInstance
import hextant.bundle.CorePermissions.Public
import hextant.main.HextantApplication
import hextant.undo.UndoManager
import hextant.undo.UndoManager.Companion
import javafx.scene.Parent

class HackusGuiTest : HextantApplication() {
    override fun createView(context: Context): Parent {
        val defs = EditableDefinitionList()
        return context.createView(defs)
    }

    override fun createContext(platform: HextantPlatform): Context = newInstance(platform) {
        set(Public, UndoManager, UndoManager.newInstance())
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(HackusGuiTest::class.java, *args)
        }
    }
}
