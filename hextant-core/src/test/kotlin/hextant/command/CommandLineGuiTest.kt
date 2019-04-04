/**
 *@author Nikolaus Knop
 */

package hextant.command

import hextant.Context
import hextant.bundle.Bundle
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
        stage.scene = hextantScene(::createContent) { platform ->
            Context.newInstance(platform)
        }
        stage.width = 1000.0
        stage.height = 1000.0
        stage.show()
    }

    companion object {
        private fun createContent(context: Context): Parent {
            val commands = Data.commands.toMutableSet()
            val targets = mutableSetOf(Receiver(true))
            val editableFactory = context[EditableFactory]
            editableFactory.run {
                register(IntLiteral::class) { -> EditableIntLiteral() }
                register(IntLiteral::class) { v -> EditableIntLiteral(v.value) }
            }
            val views = context[EditorControlFactory]
            views.run {
                register { e: EditableIntLiteral, ctx, args -> FXIntLiteralEditorView(e, ctx, args) }
            }
            val commandLine = CommandLine({ commands }, { targets }, context)
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
