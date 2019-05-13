/**
 *@author Nikolaus Knop
 */

package hextant.command

import hextant.*
import hextant.bundle.Bundle
import hextant.command.Data.Receiver
import hextant.command.line.CommandLine
import hextant.command.line.FXCommandLineView
import hextant.expr.edited.IntLiteral
import hextant.expr.editor.IntLiteralEditor
import hextant.expr.view.FXIntLiteralEditorView
import hextant.fx.hextantScene
import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.layout.HBox
import javafx.stage.Stage

class CommandLineGuiTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = hextantScene(::createContent) { platform ->
            Context.newInstance(platform)
        }
        stage.width = 1000.0
        stage.height = 1000.0
        stage.show()
    }

    companion object {
        private fun createContent(context: Context): Parent {
            val commands = { Data.commands }
            val targets = mutableSetOf(Receiver(true))
            val editableFactory = context[EditorFactory]
            editableFactory.run {
                register(IntLiteral::class) { context -> IntLiteralEditor(context) }
                register(IntLiteral::class) { v, context -> IntLiteralEditor(v.value, context) }
            }
            val views = context[EditorControlFactory]
            views.run {
                register(IntLiteralEditor::class) { e: IntLiteralEditor, args ->
                    FXIntLiteralEditorView(e, args)
                }
            }
            val commandLine = CommandLine(targets, commands, context)
            val commandLineView = FXCommandLineView(commandLine, context, Bundle.newInstance())
            val root = HBox(commandLineView)
            root.style = "-fx-background-color: black"
            return root
        }

        @JvmStatic fun main(args: Array<String>) {
            launch(CommandLineGuiTest::class.java, *args)
        }
    }
}
