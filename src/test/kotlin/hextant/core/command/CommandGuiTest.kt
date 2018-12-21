/**
 *@author Nikolaus Knop
 */

package hextant.core.command

import hextant.HextantPlatform
import hextant.core.EditableFactory
import hextant.core.command.Command.Category
import hextant.core.command.gui.commandContextMenu
import hextant.core.command.gui.commandMenuBar
import hextant.core.expr.editable.EditableIntLiteral
import hextant.core.expr.edited.IntLiteral
import hextant.get
import javafx.application.Application
import javafx.geometry.Side
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.input.*
import javafx.scene.layout.VBox
import javafx.stage.Stage

internal class CommandGuiTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = Scene(createContent())
        stage.setOnHidden { System.exit(0) }
        stage.show()
    }

    private object Receiver
    companion object {
        private fun createContent(): Parent {
            val platform = HextantPlatform.configured()
            val editableFactory = platform[EditableFactory]
            editableFactory.apply {
                register(IntLiteral::class) { -> EditableIntLiteral() }
            }
            val commands = Commands.newInstance()
            val registrar = commands.of<Receiver>().apply {
                register(command<Receiver, Unit> {
                    executing { _, _ -> println("1") }
                    description = "prints 1"
                    name = "Print 1"
                    shortName = "p1"
                    category = Category.EDIT
                }, shortcut = KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN))
                register(command<Receiver, Unit> {
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
                }, shortcut = KeyCodeCombination(KeyCode.A, KeyCombination.SHORTCUT_DOWN))
            }
            val menuBar = Receiver.commandMenuBar(platform)
            val contextMenu = Receiver.commandContextMenu(platform)
            val button = Button("Receiver")
            button.setOnContextMenuRequested { contextMenu.show(button, Side.RIGHT, 0.0, 0.0) }
            registrar.listen(button, Receiver, platform)
            return VBox(menuBar, button)
        }

        @JvmStatic fun main(args: Array<String>) {
            launch(CommandGuiTest::class.java, *args)
        }
    }
}
