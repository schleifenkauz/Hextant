/**
 *@author Nikolaus Knop
 */

@file:Suppress("RedundantLambdaArrow")

package org.nikok.hextant.core.command.line

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
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.base.EditorControl
import org.nikok.hextant.core.command.Command
import org.nikok.hextant.core.command.Command.Parameter
import org.nikok.hextant.core.command.gui.argumentEditor
import org.nikok.hextant.core.completion.Completion
import org.nikok.hextant.core.completion.gui.CompletionPopup
import org.nikok.hextant.core.fx.*
import org.nikok.reaktive.event.subscribe

internal class FXCommandLineView internal constructor(
    commandLine: CommandLine,
    platform: HextantPlatform
) : EditorControl<VBox>(), CommandLineView {
    private val historyView = ListView<CommandApplication<Any>>().apply {
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

    private inner class CommandApplicationCell : ListCell<CommandApplication<Any>>() {
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
                controller.commandLine.resume(item)
                this@FXCommandLineView.requestFocus()
            }
        }

        override fun updateItem(item: CommandApplication<Any>?, empty: Boolean) {
            super.updateItem(item, empty)
            if (item == null || empty) return
            text = item.toString()
        }
    }

    private val textField = nameTextField("")

    private fun nameTextField(t: String) = HextantTextField().apply {
        text = t
        textProperty().addListener { _, _, new -> controller.commandLine.setText(new) }
        addEventHandler(KeyEvent.KEY_RELEASED) { k ->
            if (SHOW_COMPLETIONS_SHORTCUT.match(k) || k.code == SPACE) {
                controller.showCompletions(this@FXCommandLineView)
                k.consume()
            }
        }
        textProperty().addListener { _, _, new -> pseudoClassStateChanged(PseudoClasses.ERROR, new.isEmpty()) }
        pseudoClassStateChanged(PseudoClasses.ERROR, text.isEmpty())
    }

    private val argEditors = HBox()

    private fun createCurrentCommand(): HBox {
        return HBox(textField, argEditors).apply {
            addEventHandler(KeyEvent.KEY_RELEASED) { e ->
                when {
                    EXECUTE_SHORTCUT.match(e) -> {
                        controller.commandLine.executeOrExpand()
                        e.consume()
                    }
                    RESET_SHORTCUT.match(e)   -> {
                        controller.commandLine.reset()
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

    private val controller = CommandLineController(commandLine, this, platform)

    override fun createDefaultRoot(): VBox {
        val currentCommand = createCurrentCommand()
        return VBox(historyView, currentCommand)
    }

    init {
        controller.addView(this)
        initialize(commandLine, controller, platform)
    }

    private val completionsPopup = CompletionPopup<Command<*, *>>()

    private val completionsSubscription = completionsPopup.completionChosen.subscribe("On completion chosen") { c ->
        controller.commandLine.setText(c.completed.shortName!!)
        controller.commandLine.executeOrExpand()
    }

    override fun requestFocus() {
        Platform.runLater {
            if (textField.isEditable) {
                textField.requestFocus()
            } else {
                val argEditor = argEditors.children.first() as Label
                argEditor.graphic.requestFocus()
            }
        }
    }

    fun dispose() {
        completionsSubscription.cancel()
    }


    override fun editingArgs(name: String, parameters: List<Parameter>, views: List<EditorControl<*>>) {
        textField.isEditable = false
        val nodes = views.zip(parameters) { v, p -> argumentEditor(p, v) }
        argEditors.children.addAll(nodes)
        completionsPopup.hide()
        requestFocus()
    }

    override fun setText(newText: String) {
        textField.text = newText
    }

    override fun editingName(name: String) {
        textField.isEditable = true
        argEditors.children.clear()
        requestFocus()
    }

    override fun showCompletions(completions: Set<Completion<Command<*, *>>>) {
        completionsPopup.setCompletions(completions)
        completionsPopup.show(textField)
    }

    override fun executed(appl: CommandApplication<Any>) {
        val history = historyView.items
        history.add(appl)
        if (history.size > 100) {
            history.removeAt(0)
        }
        historyView.scrollTo(history.lastIndex)
        completionsPopup.hide()
    }

    companion object {
        private val EXECUTE_SHORTCUT = KeyCodeCombination(ENTER, SHORTCUT_ANY)

        private val RESET_SHORTCUT = KeyCodeCombination(R, SHORTCUT_DOWN)

        private val SHOW_COMPLETIONS_SHORTCUT = KeyCodeCombination(SPACE, SHORTCUT_DOWN)

        private const val MAX_HISTORY_HEIGHT = 300

        private const val ROW_HEIGHT = 20
    }
}