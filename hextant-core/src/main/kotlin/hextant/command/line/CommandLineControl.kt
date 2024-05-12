/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import bundles.Bundle
import bundles.Property
import bundles.publicProperty
import hextant.codegen.ProvideImplementation
import hextant.command.Command
import hextant.command.line.CommandLine.HistoryItem
import hextant.completion.gui.CompletionPopup
import hextant.context.ControlFactory
import hextant.context.createControl
import hextant.core.Editor
import hextant.core.view.EditorControl
import hextant.fx.*
import javafx.application.Platform
import javafx.scene.control.ScrollPane
import javafx.scene.control.ScrollPane.ScrollBarPolicy.ALWAYS
import javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import reaktive.Observer
import reaktive.observe

/**
 * A JavaFX implementation of the [CommandLineView]
 */
class CommandLineControl @ProvideImplementation(ControlFactory::class) constructor(
    private val cl: CommandLine, args: Bundle
) : CommandLineView, EditorControl<Pane>(cl, args) {
    private val history = VBox().withStyleClass("command-history")
    private val scrollPane = ScrollPane(history).apply {
        hbarPolicy = NEVER
        vbarPolicy = ALWAYS
        maxHeight = arguments[HISTORY_ITEMS] * HISTORY_ITEM_HEIGHT
    }
    private val commandName = HextantTextField().withStyleClass("command-name")
    private val current = HBox(5.0, commandName).withStyleClass("command-input")

    private val popup = CompletionPopup(context, cl) { CommandCompleter }

    private val completionObserver: Observer
    private val textObserver: Observer

    init {
        registerShortcuts {
            on("Ctrl + R") {
                cl.reset()
                runFXWithTimeout { receiveFocus() }
            }
            on("Ctrl + Enter") {
                cl.execute()
                runFXWithTimeout { receiveFocus() }
            }
            on("Enter") {
                cl.expand()
            }
            on("Ctrl + Space") {
                popup.show(commandName)
            }
            on("Ctrl + Up") {
                history.children.lastOrNull()?.requestFocus()
            }
        }
        completionObserver = popup.completionChosen.observe { _, completion ->
            cl.setCommandName(completion.completionText)
            cl.expand(completion.item)
        }
        textObserver = commandName.userUpdatedText.observe(this) { _, txt ->
            cl.setCommandName(txt)
            popup.updateInput(txt)
            popup.show(commandName)
        }
        cl.addView(this)
    }

    override fun displayCommandName(name: String) {
        commandName.smartSetText(name)
    }

    override fun <T : Any> argumentChanged(property: Property<T, *>, value: T) {
        when (property) {
            HISTORY_ITEMS -> {
                val count = value as Int
                when {
                    count <= 0 -> root.children.remove(scrollPane)
                    count == 1 -> {
                        root.children.remove(scrollPane)
                        val last = history.children.lastOrNull()
                        if (last != null) root.children.add(0, last)
                    }
                    else       -> {
                        if (scrollPane !in root.children) root.children.add(0, scrollPane)
                        scrollPane.maxHeight = arguments[HISTORY_ITEMS] * HISTORY_ITEM_HEIGHT
                    }
                }
            }
        }
    }

    override fun expanded(command: Command<*, *>, editors: List<Editor<*>>) {
        commandName.text = command.shortName!!
        commandName.isEditable = false
        if (editors.isNotEmpty()) {
            val controls = editors.map { context.createControl(it) }
            setChildren(controls)
            current.children.addAll(controls)
            Platform.runLater { controls.first().receiveFocus() }
        }
    }

    override fun reset() {
        commandName.text = ""
        current.children.setAll(commandName)
        commandName.isEditable = true
        popup.updateInput("")
    }

    override fun addToHistory(item: HistoryItem) {
        val (command, arguments, byShortcut, result) = item
        val box = HBox(5.0).withStyleClass("command-history-item")
        box.isFocusTraversable = true
        box.onAction { cl.resume(command, arguments.map { (_, snapshot) -> snapshot }) }
        box.setOnMouseClicked { box.requestFocus() }
        box.children {
            +hextantLabel(command.shortName!!)
            for ((param, arg) in command.parameters.zip(arguments)) {
                +hextantLabel(arg.first.toString()).withTooltip(param.toString())
            }
            if (byShortcut) +hextantLabel("(${item.command.shortcut})")
            if (result != Unit) {
                +hextantLabel(" -> ")
                +hextantLabel(result.toString())
            }
        }
        history.children.add(box)
        if (this.arguments[HISTORY_ITEMS] == 1) {
            if (root.children.size == 2) root.children[0] = box
            else root.children.add(0, box)
        } else if (!root.children.contains(scrollPane)) {
            root.children.add(0, scrollPane)
        }
        runFXWithTimeout { scrollPane.vvalue = 1.0 }
    }

    override fun receiveFocus() {
        if (current.children.size > 1) {
            val editor = current.children[1] as EditorControl<*>
            editor.requestFocus()
        } else commandName.requestFocus()
    }

    override fun createDefaultRoot(): Pane = vbox {
        if (arguments[HISTORY_ITEMS] == 1 && history.children.isNotEmpty()) add(history.children.last())
        else if (arguments[HISTORY_ITEMS] > 1 && history.children.isNotEmpty()) add(scrollPane)
        add(current)
    }

    companion object {
        private const val HISTORY_ITEM_HEIGHT = 32.0

        /**
         * Indicates whether the history box should be shown.
         */
        val HISTORY_ITEMS = publicProperty("show-history", default = 5)
    }
}