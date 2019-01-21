/**
 *@author Nikolaus Knop
 */

package hextant.lisp

import hextant.*
import hextant.bundle.CorePermissions.Public
import hextant.bundle.CoreProperties.editorParentRegion
import hextant.fx.hextantScene
import hextant.lisp.editable.ExpandableSExpr
import hextant.lisp.editor.LispProperties.Internal
import hextant.lisp.editor.LispProperties.fileScope
import hextant.plugin.PluginRegistry
import hextant.undo.UndoManager
import hextant.undo.UndoManagerImpl
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
        stage.scene = hextantScene(createContent())
        stage.show()
    }

    companion object {
        private fun createContent(): Parent {
            val platform = HextantPlatform.configured()
            val cl = LispEditorTest::class.java.classLoader
            val pluginRegistry = platform[Public, PluginRegistry]
            pluginRegistry.loadPluginFromClasspath(cl)
            val context = object : AbstractContext(platform) {
                override val platform: HextantPlatform
                    get() = platform
            }
            val expandable = ExpandableSExpr()
            val view = context.createView(expandable)
            context[Public, editorParentRegion] = view
            context[Internal, fileScope] = FileScope.empty
            context[Public, UndoManager] = UndoManagerImpl()
            val eval = Button("Evaluate").apply {
                setOnAction {
                    val edited = expandable.edited.now ?: return@setOnAction
                    val msg = try {
                        edited.evaluate().toString()
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
