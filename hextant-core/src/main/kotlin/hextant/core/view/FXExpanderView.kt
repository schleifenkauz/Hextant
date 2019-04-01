/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import hextant.*
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.completion.Completion
import hextant.completion.gui.CompletionPopup
import hextant.core.editable.Expandable
import hextant.core.editor.Expander
import hextant.fx.*
import javafx.scene.Node
import javafx.scene.input.*
import javafx.scene.input.KeyCode.R
import javafx.scene.input.KeyCombination.SHORTCUT_DOWN
import reaktive.event.Subscription
import reaktive.event.subscribe
import reaktive.value.now

class FXExpanderView(
    private val expandable: Expandable<*, *>,
    private val context: Context,
    args: Bundle
) : ExpanderView, EditorControl<Node>(args) {

    private val expander: Expander<*, *> = context.getEditor(expandable)

    private var view: EditorControl<*>? = null

    private val textField = HextantTextField()

    private val textSubscription: Subscription

    private val completionsPopup = CompletionPopup<String>()

    private val completionSubscription: Subscription

    override fun createDefaultRoot(): Node = textField

    init {
        with(textField) {
            setOnAction { expander.expand() }
            textSubscription = userUpdatedText.subscribe { new -> expander.setText(new) }
            registerShortcut(KeyCodeCombination(KeyCode.SPACE, KeyCombination.SHORTCUT_DOWN)) {
                expander.suggestCompletions()
            }
        }
        initialize(expandable, expander, context)
        expander.addView(this)
        completionSubscription = completionsPopup.completionChosen.subscribe { comp ->
            expander.setText(comp.text)
            expander.expand()
        }
    }

    override fun textChanged(newText: String) {
        if (newText != textField.text) {
            textField.text = newText
            expander.suggestCompletions()
        }
    }

    override fun reset() {
        view = null
        root = textField
        textField.requestFocus()
        textField.text = ""
    }

    override fun expanded(newContent: Editable<*>) {
        val v = context.createView(newContent)
        view = v
        v.registerShortcut(RESET_SHORTCUT) { expander.reset() }
        root = v
        v.focus()
    }

    override fun suggestCompletions(completions: Set<Completion<String>>) {
        completionsPopup.setCompletions(completions)
        completionsPopup.show(this)
    }

    override fun requestFocus() {
        if (expandable.isExpanded.now) view!!.focus()
        else textField.requestFocus()
    }

    companion object {
        private val RESET_SHORTCUT = KeyCodeCombination(R, SHORTCUT_DOWN)
    }
}