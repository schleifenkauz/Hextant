/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.view

import javafx.scene.control.Control
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode.R
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination.SHORTCUT_ANY
import org.nikok.hextant.Editable
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.EditorViewFactory
import org.nikok.hextant.core.editable.Expandable
import org.nikok.hextant.core.editor.Expander
import org.nikok.hextant.core.fx.*
import org.nikok.reaktive.value.now

class FXExpanderView<E : Editable<*>, Ex : Expandable<*, E>>(
    private val expandable: Ex,
    private val expander: Expander<E>,
    private val views: EditorViewFactory = HextantPlatform[Public, EditorViewFactory]
) : ExpanderView, FXEditorView, Control() {

    private var view: FXEditorView? = null

    private val textField = createExpanderTextField(expandable.text.now)

    init {
        styleClass.add("expander")
        textField.initSelection(expander)
        expander.addView(this)
    }

    override fun select(isSelected: Boolean) {
        if (isSelected) {
            textField.style = "-fx-background-color: #292929;"
        } else {
            textField.style = null
        }
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
        setRoot(textField)
        textField.requestFocus()
    }

    override fun expanded(newContent: Editable<*>) {
        val v = views.getFXView(newContent)
        view = v
        v.node.registerShortcut(RESET_SHORTCUT) { expander.reset() }
        setRoot(v.node)
        v.focus()
    }

    override val node: Control
        get() = this

    override fun requestFocus() {
        if (expandable.isExpanded.now) view!!.focus()
        else textField.requestFocus()
    }

    companion object {
        private val RESET_SHORTCUT = KeyCodeCombination(R, SHORTCUT_ANY)
    }
}