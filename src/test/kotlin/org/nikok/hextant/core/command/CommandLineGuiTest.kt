/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.command

import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.layout.HBox
import javafx.stage.Stage
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.*
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.command.Data.Receiver
import org.nikok.hextant.core.command.line.CommandLine
import org.nikok.hextant.core.command.line.FXCommandLineView
import org.nikok.hextant.core.expr.editable.EditableIntLiteral
import org.nikok.hextant.core.expr.edited.IntLiteral
import org.nikok.hextant.core.expr.view.FXIntLiteralEditorView
import org.nikok.hextant.core.impl.scene

class CommandLineGuiTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = scene(createContent())
        stage.width = 1000.0
        stage.height = 1000.0
        stage.show()
    }

    companion object {
        private fun createContent(): Parent {
            val commands = Data.commands.toMutableSet()
            val targets = mutableSetOf(Receiver(true))
            val editableFactory = HextantPlatform[Public, EditableFactory]
            editableFactory.run {
                register(IntLiteral::class) { -> EditableIntLiteral() }
                register(IntLiteral::class) { v -> EditableIntLiteral(v.value) }
            }
            val views = HextantPlatform[Public, EditorViewFactory]
            views.run {
                registerFX { e: EditableIntLiteral -> FXIntLiteralEditorView(e) }
            }
            val commandLine = CommandLine({ commands }, { targets })
            val commandLineView = FXCommandLineView(commandLine, views)
            val root = HBox(commandLineView)
            root.style = "-fx-background-color: black"
            return root
        }

        @JvmStatic fun main(args: Array<String>) {
            launch(CommandLineGuiTest::class.java, *args)
        }
    }
}
