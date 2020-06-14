/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import bundles.Bundle
import hextant.Editor
import hextant.command.Command
import hextant.command.line.CommandLine.HistoryItem
import hextant.completion.gui.CompletionPopup
import hextant.createView
import hextant.fx.*
import hextant.impl.observe
import javafx.application.Platform
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

/**
 * A JavaFX implementation of the [CommandLineView]
 */
class CommandLineControl(private val cl: CommandLine, args: Bundle) : CommandLineView, EditorControl<VBox>(cl, args) {
    private val history = VBox().withStyleClass("command-history")
    private val commandName = HextantTextField().withStyleClass("command-name")
    private val current = HBox(commandName).withStyleClass("command-input")

    private val completer = CommandCompleter { cl.availableCommands() }
    private val popup = CompletionPopup(Unit, context[IconManager], completer)

    private val completionObserver = popup.completionChosen.observe { _, completion ->
        cl.setCommandName(completion.completionText)
        cl.expand(completion.completion)
    }

    private val textObserver = commandName.userUpdatedText.observe(this) { _, txt ->
        cl.setCommandName(txt)
        popup.updateInput(txt)
        popup.show(this)
    }

    init {
        cl.addView(this)
        registerShortcuts {
            on("Ctrl + R") {
                cl.reset()
            }
            on("Ctrl + Enter") {
                cl.execute()
            }
            on("Enter") {
                cl.expand()
            }
            on("Ctrl + Space") {
                popup.show(this@CommandLineControl)
            }
            on("Ctrl + Up") {
                history.children.lastOrNull()?.requestFocus()
            }
        }
    }

    override fun displayCommandName(name: String) {
        commandName.smartSetText(name)
    }

    override fun expanded(command: Command<*, *>, editors: List<Editor<*>>) {
        commandName.text = command.shortName!!
        commandName.isEditable = false
        val controls = editors.map { context.createView(it) }
        current.children.addAll(controls)
        Platform.runLater { controls.firstOrNull()?.receiveFocus() }
    }

    override fun reset() {
        commandName.text = ""
        current.children.setAll(commandName)
        commandName.isEditable = true
    }

    override fun addToHistory(item: HistoryItem) {
        val (command, arguments, result) = item
        val box = HBox().withStyleClass("command-history-item")
        box.isFocusTraversable = true
        box.onAction { cl.resume(command, arguments.map { (_, snapshot) -> snapshot }) }
        box.setOnMouseClicked { box.requestFocus() }
        with(box.children) {
            add(label(command.shortName!!))
            add(label(" "))
            for ((param, arg) in command.parameters.zip(arguments)) {
                add(label(arg.first.toString()).withTooltip(param.toString()))
            }
            if (result != Unit) {
                add(label(" -> "))
                add(label(result.toString()))
            }
        }
        history.children.add(box)
    }

    override fun receiveFocus() {
        if (current.children.size > 1) {
            val editor = current.children[1] as EditorControl<*>
            editor.requestFocus()
        } else commandName.requestFocus()
    }

    override fun createDefaultRoot(): VBox = VBox(history, current)
}