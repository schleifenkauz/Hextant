/**
 *@author Nikolaus Knop
 */

package hextant.command.gui

import hextant.*
import hextant.base.EditorControl
import hextant.command.Command
import hextant.command.Command.Parameter
import hextant.core.EditableFactory
import hextant.fx.UtilityDialog
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.ContentDisplay.RIGHT
import javafx.scene.layout.VBox
import javafx.stage.Window
import reaktive.value.now

fun showArgumentPrompt(
    ownerWindow: Window,
    command: Command<*, *>,
    context: Context
): List<Any?>? {
    val parameters = command.parameters
    val editableFactory = context[EditableFactory]
    val editableArguments = parameters.map { p -> p to editableFactory.getEditable(p.type) }
    val views = editableArguments.map { (p, e) -> p to context.createView(e) }
    val vbox = VBox()
    for ((p, v) in views) {
        vbox.children.add(argumentEditor(p, v))
    }
    val bt = UtilityDialog.show {
        title = command.name
        headerText = command.description
        content = vbox
        owner = ownerWindow
    }
    return when (bt) {
        null -> null
        ButtonType.CANCEL -> null
        else -> editableArguments.map { (_, e) -> e.edited.now }
    }
}

internal fun argumentEditor(parameter: Parameter, view: EditorControl<*>): Node =
    Label("${parameter.name}: ", view).apply {
            tooltip = Tooltip(parameter.toString())
            contentDisplay = RIGHT
        }
