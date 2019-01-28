/**
 *@author Nikolaus Knop
 */

package hextant.command

import hextant.HextantPlatform
import hextant.command.Data.Receiver
import hextant.command.line.CommandLine
import hextant.command.line.FXCommandLineView
import hextant.core.*
import hextant.expr.editable.EditableIntLiteral
import hextant.expr.edited.IntLiteral
import hextant.expr.view.FXIntLiteralEditorView
import hextant.fx.hextantScene
import hextant.get
import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.layout.HBox
import javafx.stage.Stage

class CommandLineGuiTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = hextantScene(createContent())
        stage.width = 1000.0
        stage.height = 1000.0
        stage.show()
    }

    companion object {
        private fun createContent(): Parent {
            val commands = Data.commands.toMutableSet()
            val targets = mutableSetOf(Receiver(true))
            val platform = HextantPlatform.configured()
            val editableFactory = platform[EditableFactory]
            editableFactory.run {
                register(IntLiteral::class) { -> EditableIntLiteral() }
                register(IntLiteral::class) { v -> EditableIntLiteral(v.value) }
            }
            val views = platform[EditorControlFactory]
            views.run {
                register { e: EditableIntLiteral, ctx -> FXIntLiteralEditorView(e, ctx) }
            }
            val commandLine = CommandLine({ commands }, { targets }, platform)
            val commandLineView = FXCommandLineView(commandLine, platform)
            val root = HBox(commandLineView)
            root.style = "-fx-background-color: black"
            return root
        }

        @JvmStatic fun main(args: Array<String>) {
            launch(CommandLineGuiTest::class.java, *args)
        }
    }
}
