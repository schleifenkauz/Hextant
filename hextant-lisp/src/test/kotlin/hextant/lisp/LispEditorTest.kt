/**
 *@author Nikolaus Knop
 */

package hextant.lisp

import hextant.*
import hextant.bundle.CorePermissions.Public
import hextant.bundle.CoreProperties.editorParentRegion
import hextant.fx.hextantScene
import hextant.lisp.editor.LispProperties.fileScope
import hextant.lisp.editor.SExprExpander
import hextant.plugin.PluginRegistry
import hextant.undo.UndoManager
import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.INFORMATION
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.stage.Stage
import reaktive.value.now

class LispEditorTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = hextantScene(::createContent) { platform ->
            Context.newInstance(platform) {
                set(Public, fileScope, FileScope.empty)
                set(Public, UndoManager, UndoManager.newInstance())
            }
        }
        stage.show()
    }

    companion object {
        private fun createContent(context: Context): Parent {
            val cl = LispEditorTest::class.java.classLoader
            val pluginRegistry = context[Public, PluginRegistry]
            pluginRegistry.loadPluginFromClasspath(cl)
            val expandable = SExprExpander(context)
            val view = context.createView(expandable)
            context[Public, editorParentRegion] = view
            val eval = Button("Evaluate").apply {
                setOnAction {
                    val result = expandable.result.now.orNull() ?: return@setOnAction
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

        @JvmStatic
        fun main(args: Array<String>) {
            launch(LispEditorTest::class.java, *args)
        }
    }
}
