/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.command

import javafx.application.Application
import javafx.geometry.Side
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.input.*
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.EditableFactory
import org.nikok.hextant.core.EditorViewFactory
import org.nikok.hextant.core.command.Command.Category
import org.nikok.hextant.core.command.gui.commandContextMenu
import org.nikok.hextant.core.command.gui.commandMenuBar
import org.nikok.hextant.core.expr.editable.EditableIntLiteral
import org.nikok.hextant.core.expr.edited.IntLiteral
import org.nikok.hextant.core.expr.view.FXIntLiteralEditorView
import org.nikok.hextant.prop.CorePermissions.Public

internal class CommandGuiTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = Scene(createContent())
        stage.show()
    }

    private object Receiver
    companion object {
        private fun createContent(): Parent {
            val editableFactory = HextantPlatform[Public, EditableFactory]
            editableFactory.apply {
                register(IntLiteral::class) { -> EditableIntLiteral() }
            }
            val viewFactory = HextantPlatform[Public, EditorViewFactory]
            viewFactory.apply {
                registerFX(EditableIntLiteral::class, ::FXIntLiteralEditorView)
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
            val menuBar = Receiver.commandMenuBar(registrar)
            val contextMenu = Receiver.commandContextMenu(registrar)
            val button = Button("Receiver")
            button.setOnContextMenuRequested { contextMenu.show(button, Side.RIGHT, 0.0, 0.0) }
            registrar.listen(button, Receiver)
            return VBox(menuBar, button)
        }

        @JvmStatic fun main(args: Array<String>) {
            launch(CommandGuiTest::class.java, *args)
        }
    }
}
