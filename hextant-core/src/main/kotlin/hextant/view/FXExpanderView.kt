/**
 *@author Nikolaus Knop
 */

package hextant.view

import hextant.*
import hextant.base.EditorControl
import hextant.core.editable.Expandable
import hextant.core.editor.Expander
import hextant.fx.HextantTextField
import hextant.fx.registerShortcut
import javafx.scene.Node
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode.R
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination.SHORTCUT_ANY
import org.nikok.reaktive.value.now

class FXExpanderView(
    private val expandable: Expandable<*, *>,
    private val context: Context
) : ExpanderView, EditorControl<Node>() {

    private val expander = context.getEditor(expandable) as Expander<*, *>

    private var view: EditorControl<*>? = null

    private val textField = createExpanderTextField(expandable.text.now)

    override fun createDefaultRoot(): Node = textField

    init {
        initialize(expandable, expander, context)
        expander.addView(this)
    }

    private fun createExpanderTextField(text: String?): TextField = HextantTextField(text).apply {
        setOnAction { expander.expand() }
        textProperty().addListener { _, _, new -> expander.setText(new) }
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
        private val RESET_SHORTCUT = KeyCodeCombination(R, SHORTCUT_ANY)
    }
}