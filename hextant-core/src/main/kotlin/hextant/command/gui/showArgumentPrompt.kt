/**
 *@author Nikolaus Knop
 */

package hextant.command.gui

import hextant.*
import hextant.command.Command
import hextant.command.Command.Parameter
import hextant.fx.EditorControl
import hextant.fx.UtilityDialog
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.ContentDisplay.RIGHT
import javafx.scene.layout.VBox
import javafx.stage.Window
import reaktive.value.now

/**
 * Show a prompt with argument editors for the parameters of the given [command].
 */
fun showArgumentPrompt(
    ownerWindow: Window,
    command: Command<*, *>,
    context: Context
): List<Any?>? {
    val parameters = command.parameters
    val editableFactory = context[EditorFactory]
    val editableArguments = parameters.map { p -> p to editableFactory.createEditor(p.type, context) }
    val views = editableArguments.map { (p, e) -> p to context.createView(e) }
    val vbox = VBox()
    for ((p, v) in views) {
        vbox.children.add(argumentEditor(p, v))
    }
    val bt = UtilityDialog.show(context) {
        title = command.name
        headerText = command.description
        content = vbox
        owner = ownerWindow
    }
    return when (bt) {
        null              -> null
        ButtonType.CANCEL -> null
        else              -> editableArguments.map { (_, e) -> e.result.now }
    }
}

internal fun argumentEditor(parameter: Parameter, view: EditorControl<*>): Node =
    Label("${parameter.name}: ", view).apply {
        tooltip = Tooltip(parameter.toString())
        contentDisplay = RIGHT
    }
