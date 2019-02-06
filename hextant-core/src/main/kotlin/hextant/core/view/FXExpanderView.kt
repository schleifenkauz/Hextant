/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import hextant.*
import hextant.base.EditorControl
import hextant.core.editable.Expandable
import hextant.core.editor.Expander
import hextant.fx.HextantTextField
import hextant.fx.registerShortcut
import javafx.scene.Node
import javafx.scene.input.KeyCode.R
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination.SHORTCUT_DOWN
import reaktive.event.Subscription
import reaktive.event.subscribe
import reaktive.value.now

class FXExpanderView(
    private val expandable: Expandable<*, *>,
    private val context: Context
) : ExpanderView, EditorControl<Node>() {

    private val expander = context.getEditor(expandable) as Expander<*, *>

    private var view: EditorControl<*>? = null

    private val textField = HextantTextField()

    private val textSubscription: Subscription

    override fun createDefaultRoot(): Node = textField

    init {
        with(textField) {
            setOnAction { expander.expand() }
            textSubscription = userUpdatedText.subscribe { new -> expander.setText(new) }
        }
        initialize(expandable, expander, context)
        expander.addView(this)
    }

    override fun textChanged(newText: String) {
        if (newText != textField.text) {
            textField.text = newText
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

    override fun requestFocus() {
        if (expandable.isExpanded.now) view!!.focus()
        else textField.requestFocus()
    }

    companion object {
        private val RESET_SHORTCUT = KeyCodeCombination(R, SHORTCUT_DOWN)
    }
}