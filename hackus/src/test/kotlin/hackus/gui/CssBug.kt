/**
 *@author Nikolaus Knop
 */

package hackus.gui

import hackus.editable.EditableFQName
import hextant.Context
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Public
import hextant.core.view.FXTokenEditorView
import hextant.fx.hextantScene
import hextant.undo.UndoManager
import javafx.application.Application
import javafx.scene.Parent
import javafx.stage.Stage

class CssBug : Application() {
    override fun start(stage: Stage) {
        stage.scene = hextantScene(::createContent) { platform ->
            Context.newInstance(platform) {
                set(Public, UndoManager, UndoManager.newInstance())
            }
        }
        stage.show()
    }

    companion object {
        private fun createContent(context: Context): Parent =
            object : FXTokenEditorView(EditableFQName(), context, Bundle.newInstance()) {
                init {
                    error(true)
                }

                override fun receiveFocus() {
                    root.requestFocus()
                }
            }

        @JvmStatic
        fun main(args: Array<String>) {
            launch(CssBug::class.java, *args)
        }
    }
}
