/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import bundles.SimpleProperty
import hextant.*
import hextant.completion.Completer
import hextant.completion.NoCompleter
import hextant.completion.gui.CompletionPopup
import hextant.core.editor.Expander
import hextant.fx.*
import hextant.main.InputMethod
import javafx.scene.Node
import reaktive.Observer
import reaktive.value.now

/**
 * JavaFX implementation of a [ExpanderView]
 */
open class FXExpanderView(
    private val expander: Expander<*, *>,
    args: Bundle
) : ExpanderView, EditorControl<Node>(expander, args) {
    constructor(expander: Expander<*, *>, args: Bundle, completer: Completer<Context, Any>) :
            this(expander, args.also { it[COMPLETER] = completer })

    private var view: EditorControl<*>? = null

    private val textField = HextantTextField(initialInputMethod = context[InputMethod])

    private val textObserver: Observer

    private val popup = CompletionPopup(context, context[IconManager], context[COMPLETER])

    private val completionObserver: Observer

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
            registerShortcuts {
                on("Ctrl + Space") { popup.show(this@FXExpanderView) }
                on("Enter") { expander.expand() }
            }
            textObserver = userUpdatedText.observe { _, new ->
                expander.setText(new)
                popup.updateInput(new)
                popup.show(this)
            }
        }
        expander.editor.now?.let { showContent(it) }
        expander.text.now?.let { displayText(it) }
        expander.addView(this)
        completionObserver = popup.completionChosen.observe { _, completion ->
            expander.complete(completion)
        }
    }

    override fun displayText(text: String) {
        if (text != textField.text) {
            textField.text = text
            popup.show(this)
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

    final override fun expanded(editor: Editor<*>) {
        val v = showContent(editor)
        onExpansion(editor, v)
    }

    /**
     * Called when the [Expander] has been expanded to the given [editor].
     * The default implementation does nothing.
     */
    protected open fun onExpansion(editor: Editor<*>, control: EditorControl<*>) {}

    private fun showContent(editor: Editor<*>): EditorControl<*> {
        val v = context.createView(editor)
        view = v
        v.registerShortcuts {
            on("Ctrl? + R") { expander.reset() }
        }
        root = v
        this.next?.let { v.setNext(it) }
        this.previous?.let { v.setPrevious(it) }
        this.editorParent?.let { v.setEditorParent(it) }
        v.root //ensure that root is fully initialized
        v.receiveFocus()
        return v
    }

    companion object {
        /**
         * This property controls the completer of the expander control
         */
        val COMPLETER = SimpleProperty<Completer<Context, Any>>("completer", default = NoCompleter)
    }
}