/**
 *@author Nikolaus Knop
 */

package hextant.command

import hextant.*
import hextant.command.Command.Category
import hextant.command.gui.commandContextMenu
import hextant.command.gui.commandMenuBar
import hextant.expr.IntLiteral
import hextant.expr.editor.IntLiteralEditor
import javafx.application.Application
import javafx.geometry.Side
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.stage.Stage
import kotlin.collections.component1
import kotlin.system.exitProcess

internal class CommandGuiTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = Scene(createContent())
        stage.setOnHidden { exitProcess(0) }
        stage.show()
    }

    private object Receiver
    companion object {
        private fun createContent(): Parent {
            val platform = HextantPlatform.rootContext()
            val editableFactory = platform[EditorFactory]
            editableFactory.apply {
                register { context -> IntLiteralEditor(context) }
            }
            val commands = Commands.newInstance()
            commands.apply {
                registerCommand<Receiver, Unit> {
                    executing { _, _ -> println("1") }
                    description = "prints 1"
                    name = "Print 1"
                    shortName = "p1"
                    category = Category.EDIT
                }
                registerCommand<Receiver, Unit> {
                    name = "Print Argument"
                    shortName = "printarg"
                    description = "Prints the specified argument"
                    category = Category.EDIT
                    addParameter {
                        name = "arg"
                        ofType<IntLiteral>()
                    }
                    executing { _, (arg) ->
                        arg as IntLiteral
                        println(arg.value)
                    }
                }
            }
            val menuBar = Receiver.commandMenuBar(platform)
            val contextMenu = Receiver.commandContextMenu(platform)
            val button = Button("Receiver")
            button.setOnContextMenuRequested { contextMenu.show(button, Side.RIGHT, 0.0, 0.0) }
            return VBox(menuBar, button)
        }

        @JvmStatic fun main(args: Array<String>) {
            launch(CommandGuiTest::class.java, *args)
        }
    }
}
