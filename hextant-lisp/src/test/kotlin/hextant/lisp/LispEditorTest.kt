/**
 *@author Nikolaus Knop
 */

package hextant.lisp

import hextant.context.*
import hextant.context.CoreProperties.editorParentRegion
import hextant.lisp.editor.LispProperties.fileScope
import hextant.lisp.editor.SExprExpander
import hextant.main.HextantApplication
import javafx.scene.Parent
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.INFORMATION
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import reaktive.value.now
import validated.orNull

class LispEditorTest : HextantApplication() {
    override fun createContext(root: Context): Context = root.extend {
        set(fileScope, FileScope.empty)
    }

    override fun createView(context: Context): Parent {
        val expander = SExprExpander(context)
        val view = context.createView(expander)
        context[editorParentRegion] = view
        val eval = Button("Evaluate").apply {
            setOnAction {
                val result = expander.result.now.orNull() ?: return@setOnAction
                val msg = try {
                    result.evaluate().toString()
                } catch (e: LispRuntimeError) {
                    e.message
                }
                Alert(INFORMATION, msg).show()
            }
        }
        return VBox(eval, view)
    }


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(LispEditorTest::class.java, *args)
        }
    }
}
