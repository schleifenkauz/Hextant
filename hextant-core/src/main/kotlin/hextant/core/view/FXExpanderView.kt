/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import hextant.Editor
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.completion.Completer
import hextant.completion.NoCompleter
import hextant.completion.gui.CompleterPopupHelper
import hextant.core.editor.Expander
import hextant.createView
import hextant.fx.*
import hextant.fx.ModifierValue.DOWN
import javafx.scene.Node
import javafx.scene.input.KeyCode.*
import reaktive.event.Subscription
import reaktive.event.subscribe

/**
 * JavaFX implementation of a [ExpanderView]
 */
class FXExpanderView(
    private val expander: Expander<*, *>,
    args: Bundle,
    completer: Completer<String>
) : ExpanderView, EditorControl<Node>(expander, args) {
    constructor(expander: Expander<*, *>, args: Bundle) : this(expander, args, NoCompleter)

    private var view: EditorControl<*>? = null

    private val textField = HextantTextField()

    private val textSubscription: Subscription

    private val completionHelper = CompleterPopupHelper(completer, textField::getText)

    private val completionSubscription: Subscription

    override fun createDefaultRoot(): Node = textField

    override fun setEditorParent(parent: EditorControl<*>) {
        super.setEditorParent(parent)
        view?.setEditorParent(parent)
    }

    override fun setNext(nxt: EditorControl<*>) {
        super.setNext(nxt)
        view?.setNext(nxt)
    }

    override fun setPrevious(prev: EditorControl<*>) {
        super.setPrevious(prev)
        view?.setPrevious(prev)
    }

    init {
        with(textField) {
            setOnAction { expander.expand() }
            textSubscription = userUpdatedText.subscribe { new -> expander.setText(new) }
            registerShortcuts {
                on(shortcut(V) { control(DOWN); shift(DOWN) }) { expander.paste() }
                on(shortcut(SPACE) { control(DOWN) }) { completionHelper.show(this@FXExpanderView) }
            }
        }
        expander.addView(this)
        completionSubscription = completionHelper.completionChosen.subscribe { comp ->
            expander.setText(comp.completed)
            expander.expand()
        }
    }

    override fun displayText(text: String) {
        if (text != textField.text) {
            textField.text = text
            completionHelper.show(this)
        }
    }

    override fun receiveFocus() {
        if (view != null) view!!.receiveFocus()
        else textField.requestFocus()
    }

    override fun reset() {
        view = null
        root = textField
        textField.text = ""
        requestFocus()
    }

    override fun expanded(editor: Editor<*>) {
        val v = context.createView(editor)
        view = v
        v.registerShortcuts {
            on(shortcut(R) { control(DOWN) }) { expander.reset() }
            on(shortcut(C) { control(DOWN); shift(DOWN) }) { expander.copy() }
        }
        root = v
        this.next?.let { v.setNext(it) }
        this.previous?.let { v.setPrevious(it) }
        this.editorParent?.let { v.setEditorParent(it) }
        v.root //ensure that root is fully initialized
        v.receiveFocus()
    }
}