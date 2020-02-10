/**
 *@author Nikolaus Knop
 */

@file:Suppress("RedundantLambdaArrow")

package hextant.command.line

import hextant.*
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.command.Command.Parameter
import hextant.command.gui.argumentEditor
import hextant.completion.gui.CompletionPopup
import hextant.fx.*
import javafx.application.Platform
import javafx.beans.Observable
import javafx.scene.control.*
import javafx.scene.input.KeyCode.*
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination.SHORTCUT_ANY
import javafx.scene.input.KeyCombination.SHORTCUT_DOWN
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import reaktive.event.subscribe

class FXCommandLineView(
    private val commandLine: CommandLine,
    context: Context,
    args: Bundle
) : EditorControl<VBox>(commandLine, context, args), CommandLineView {
    private val historyView = ListView<CommandApplication>().apply {
        styleClass.add("history-view")
        prefHeight = 0.0
        prefWidth = 1000.0
        fixedCellSize = ROW_HEIGHT.toDouble()
        items.addListener { _: Observable ->
            val needed = items.size * ROW_HEIGHT
            prefHeight = Math.min(
                needed,
                MAX_HISTORY_HEIGHT
            ).toDouble()
        }
        setCellFactory { _ -> CommandApplicationCell() }
    }

    private inner class CommandApplicationCell : ListCell<CommandApplication>() {
        init {
            resumeOnEnter()
            resumeOnDoubleClick()
            isFocusTraversable = true
            styleClass.addAll("command-history-cell", "hextant-text")
        }

        private fun resumeOnDoubleClick() {
            setOnMouseClicked { e ->
                if (e.clickCount > 1) {
                    resume()
                    e.consume()
                }
            }
        }

        private fun resumeOnEnter() {
            registerShortcut(KeyCodeCombination(ENTER)) { resume() }
        }

        private fun resume() {
            if (item != null) {
                commandLine.resume(item)
                this@FXCommandLineView.focus()
            }
        }

        override fun updateItem(item: CommandApplication?, empty: Boolean) {
            super.updateItem(item, empty)
            if (item == null || empty) return
            text = item.toString()
        }
    }

    private val textField = nameTextField()

    private val textUpdater = textField.userUpdatedText.subscribe { new ->
        commandLine.editName(new)
        popup.show(this)
    }

    private fun nameTextField(): HextantTextField = HextantTextField().apply {
        addEventHandler(KeyEvent.KEY_RELEASED) { k ->
            if (SHOW_COMPLETIONS_SHORTCUT.match(k) || k.code == SPACE) {
                popup.show(this)
                k.consume()
            }
        }
    }

    private val argEditors = HBox()

    private fun createCurrentCommand(): HBox {
        return HBox(textField, argEditors).apply {
            addEventHandler(KeyEvent.KEY_RELEASED) { e ->
                when {
                    EXECUTE_SHORTCUT.match(e) -> {
                        commandLine.executeOrExpand()
                        e.consume()
                    }
                    RESET_SHORTCUT.match(e)   -> {
                        commandLine.reset()
                        e.consume()
                    }
                    e.code == UP              -> {
                        historyView.requestFocus()
                        e.consume()
                    }
                }
            }
        }
    }

    override fun createDefaultRoot(): VBox {
        val currentCommand = createCurrentCommand()
        return VBox(historyView, currentCommand)
    }

    init {
        commandLine.addView(this)
    }

    private val popup = CompletionPopup(commandLine, context[IconManager], CommandCompleter)

    private val completionsSubscription = popup.completionChosen.subscribe { c ->
        commandLine.editName(c.completion.shortName!!)
        commandLine.executeOrExpand()
    }

    override fun receiveFocus() {
        if (textField.isEditable) {
            textField.requestFocus()
        } else {
            val lbl = argEditors.children.first() as Label
            val editor = lbl.graphic as EditorControl<*>
            editor.receiveFocus()
        }
    }

    fun dispose() {
        completionsSubscription.cancel()
        textUpdater.toString()
    }


    override fun editingArguments(name: String, parameters: List<Parameter>, editors: List<Editor<*>>) {
        textField.isEditable = false
        val views = editors.map { editor -> context.createView(editor) }
        val nodes = views.zip(parameters) { v, p -> argumentEditor(p, v) }
        argEditors.children.addAll(nodes)
        popup.hide()
        Platform.runLater { receiveFocus() }
    }

    override fun displayText(newText: String) {
        textField.smartSetText(newText)
    }

    override fun editingName(name: String) {
        textField.isEditable = true
        textField.text = name
        argEditors.children.clear()
    }

    override fun executed(appl: CommandApplication) {
        val history = historyView.items
        history.add(appl)
        if (history.size > 100) {
            history.removeAt(0)
        }
        historyView.scrollTo(history.lastIndex)
        popup.hide()
    }

    companion object {
        private val EXECUTE_SHORTCUT = KeyCodeCombination(ENTER, SHORTCUT_ANY)

        private val RESET_SHORTCUT = KeyCodeCombination(R, SHORTCUT_DOWN)

        private val SHOW_COMPLETIONS_SHORTCUT = KeyCodeCombination(SPACE, SHORTCUT_DOWN)

        private const val MAX_HISTORY_HEIGHT = 300

        private const val ROW_HEIGHT = 20
    }
}