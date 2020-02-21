/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.Editor
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.command.Command
import hextant.completion.gui.CompletionPopup
import hextant.createView
import hextant.fx.*
import hextant.impl.subscribe
import javafx.application.Platform
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import reaktive.event.subscribe

/**
 * A JavaFX implemenation of the [CommandLineView]
 */
class CommandLineControl(private val cl: CommandLine, args: Bundle) : CommandLineView, EditorControl<VBox>(cl, args) {
    private val history = VBox().withStyleClass("command-history")
    private val commandName = HextantTextField().withStyleClass("command-name")
    private val current = HBox(commandName).withStyleClass("command-input")

    private val popup = CompletionPopup(cl, context[IconManager], CommandCompleter)

    private val completionSubscription = popup.completionChosen.subscribe { completion ->
        cl.setCommandName(completion.completionText)
        cl.expand(completion.completion)
    }

    private val textSubscription = commandName.userUpdatedText.subscribe(this) { _, txt ->
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
        commandName.isEditable = false
        val controls = editors.map { context.createView(it) }
        for ((param, arg) in command.parameters.zip(controls)) {
            current.children.add(arg.withTooltip(param.toString()))
        }
        Platform.runLater { controls.firstOrNull()?.receiveFocus() }
    }

    override fun reset() {
        commandName.text = ""
        current.children.setAll(commandName)
        commandName.isEditable = true
    }

    override fun addToHistory(command: Command<*, *>, arguments: List<Any>, result: Any) {
        val item = HBox().withStyleClass("command-history-item")
        item.isFocusTraversable = true
        item.onAction { cl.resume(command, arguments) }
        item.setOnMouseClicked { item.requestFocus() }
        with(item.children) {
            add(label(command.shortName!!))
            add(label(" "))
            for ((param, arg) in command.parameters.zip(arguments)) {
                add(label(arg.toString()).withTooltip(param.toString()))
            }
            if (result != Unit) {
                add(label(" -> "))
                add(label(result.toString()))
            }
        }
        history.children.add(item)
    }

    override fun createDefaultRoot(): VBox = VBox(history, current)
}