/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.command.gui

import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.ContentDisplay.RIGHT
import javafx.scene.layout.VBox
import javafx.stage.Window
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.EditableFactory
import org.nikok.hextant.core.EditorViewFactory
import org.nikok.hextant.core.command.Command
import org.nikok.hextant.core.command.Command.Parameter
import org.nikok.hextant.core.fx.FXEditorView
import org.nikok.hextant.core.fx.UtilityDialog
import org.nikok.hextant.get
import org.nikok.reaktive.value.now

fun showArgumentPrompt(
    ownerWindow: Window,
    command: Command<*, *>,
    platform: HextantPlatform
): List<Any?>? {
    val parameters = command.parameters
    val editableFactory = platform[EditableFactory]
    val editableArguments = parameters.map { p -> p to editableFactory.getEditable(p.type) }
    val viewFactory = platform[EditorViewFactory]
    val views = editableArguments.map { (p, e) -> p to viewFactory.getFXView(e) }
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

internal fun argumentEditor(parameter: Parameter, view: FXEditorView): Node =
        Label("${parameter.name}: ", view.node).apply {
            tooltip = Tooltip(parameter.toString())
            contentDisplay = RIGHT
        }
