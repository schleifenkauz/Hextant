/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.command.Command
import hextant.command.line.CommandLine.HistoryItem
import hextant.completion.gui.CompletionPopup
import hextant.context.ControlFactory
import hextant.context.createControl
import hextant.core.Editor
import hextant.core.view.EditorControl
import hextant.fx.*
import hextant.impl.observe
import javafx.application.Platform
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

/**
 * A JavaFX implementation of the [CommandLineView]
 */
class CommandLineControl @ProvideImplementation(ControlFactory::class) constructor(
    private val cl: CommandLine, args: Bundle
) : CommandLineView, EditorControl<VBox>(cl, args) {
    private val history = VBox().withStyleClass("command-history")
    private val commandName = HextantTextField().withStyleClass("command-name")
    private val current = HBox(commandName).withStyleClass("command-input")

    private val popup = CompletionPopup(context, cl) { CommandCompleter }

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
        val controls = editors.map { context.createControl(it) }
        setChildren(controls)
        current.children.addAll(controls)
        Platform.runLater { controls.firstOrNull()?.receiveFocus() }
    }

    override fun reset() {
        commandName.text = ""
        current.children.setAll(commandName)
        commandName.isEditable = true
        Platform.runLater { receiveFocus() }
    }

    override fun addToHistory(item: HistoryItem) {
        val (command, arguments, result) = item
        val box = HBox().withStyleClass("command-history-item")
        box.isFocusTraversable = true
        box.onAction { cl.resume(command, arguments.map { (_, snapshot) -> snapshot }) }
        box.setOnMouseClicked { box.requestFocus() }
        with(box.children) {
            add(hextantLabel(command.shortName!!))
            add(hextantLabel(" "))
            for ((param, arg) in command.parameters.zip(arguments)) {
                add(hextantLabel(arg.first.toString()).withTooltip(param.toString()))
            }
            if (result != Unit) {
                add(hextantLabel(" -> "))
                add(hextantLabel(result.toString()))
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