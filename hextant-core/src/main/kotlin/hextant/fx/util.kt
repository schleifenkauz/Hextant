package hextant.fx

import fxutils.KeyEventHandlerBody
import fxutils.registerShortcuts
import fxutils.runFXWithTimeout
import fxutils.showDialog
import hextant.command.Commands
import hextant.command.line.CommandLine
import hextant.context.Context
import hextant.context.EditorControlGroup
import hextant.context.createControl
import hextant.core.Editor
import hextant.core.view.EditorControl
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.stage.Stage
import reaktive.value.now

/**
 * Gets input from the user by showing the given [editor] in a [Dialog] to him.
 *
 * If the user cancels the dialog `null` is returned.
 * @param control the [EditorControl] that shows the editor to the user.
 * @param buttonTypes the possible button types.
 */
fun <R> getUserInput(
    title: String,
    editor: Editor<R>,
    control: Node = editor.context.createControl(editor),
    buttonTypes: List<ButtonType> = listOf(ButtonType.OK, ButtonType.CANCEL),
    applyStyle: Boolean = true
): R? {
    return showDialog<R> {
        dialogPane.content = control
        dialogPane.buttonTypes.setAll(buttonTypes)
        dialogPane.scene.initHextantScene(editor.context, applyStyle)
        val ok = dialogPane.lookupButton(ButtonType.OK) as Button
        ok.isDefaultButton = false
        dialogPane.registerShortcuts {
            on("Ctrl+Enter") { ok.fire() }
        }
        setResultConverter { btn ->
            when (btn) {
                ButtonType.OK -> editor.result.now
                ButtonType.CANCEL -> null
                else -> error("Unexpected button type: $btn")
            }
        }
        setOnShown {
            runFXWithTimeout {
                editor.context[EditorControlGroup].getViewOf(editor).receiveFocus()
            }
        }
        this.title = title
    }
}

fun KeyEventHandlerBody<*>.handleCommands(target: Any, context: Context, commandLine: CommandLine) {
    for (command in context[Commands].applicableOn(target)) {
        val shortcut = command.shortcut
        if (shortcut != null) {
            on(shortcut, consume = false) { ev ->
                if (command.parameters.isEmpty()) {
                    val result = command.execute(target, emptyList())
                    if (result != false) ev.consume()
                } else {
                    commandLine.expand(command)
                    context[EditorControlGroup].getViewOf(commandLine).receiveFocus()
                    ev.consume()
                }
            }
        }
    }
}

/**
 * Shows a new [Stage] with the given node as the root.
 */
fun showStage(root: Parent, context: Context, applyStyle: Boolean): Stage = Stage().apply {
    scene = Scene(root)
    scene.initHextantScene(context, applyStyle)
    show()
}

/**
 * Shows a [Stage] with the view of the given [editor] as the root.
 */
fun showStage(editor: Editor<*>, applyStyle: Boolean) =
    showStage(editor.context.createControl(editor), editor.context, applyStyle)

/**
 * Runs [EditorControl.receiveFocus] on the JavaFX application thread after the given [delay] which is measured in milliseconds.
 * @see runFXWithTimeout
 */
fun EditorControl<*>.receiveFocusLater(delay: Long = 10) {
    runFXWithTimeout(delay) { receiveFocus() }
}
