/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import hextant.Editor
import hextant.base.EditorControl
import hextant.bundle.*
import hextant.bundle.CorePermissions.Public
import hextant.completion.Completer
import hextant.completion.NoCompleter
import hextant.completion.gui.CompleterPopupHelper
import hextant.core.editor.Expander
import hextant.createView
import hextant.fx.HextantTextField
import hextant.fx.registerShortcuts
import hextant.main.InputMethod
import javafx.scene.Node
import reaktive.event.Subscription
import reaktive.event.subscribe
import reaktive.value.now

/**
 * JavaFX implementation of a [ExpanderView]
 */
open class FXExpanderView(
    private val expander: Expander<*, *>,
    args: Bundle
) : ExpanderView, EditorControl<Node>(expander, args) {
    constructor(expander: Expander<*, *>, args: Bundle, completer: Completer<String>) :
            this(expander, args.also { it[Public, COMPLETER] = completer })

    private var view: EditorControl<*>? = null

    private val textField = HextantTextField(initialInputMethod = context[Public, InputMethod])

    private val textSubscription: Subscription

    /**
     * The completer used by this FXExpanderView
     */
    var completer
        get() = arguments.getOrNull(Public, COMPLETER) ?: NoCompleter
        set(value) {
            arguments[Public, COMPLETER] = value
        }

    override fun argumentChanged(property: Property<*, *, *>, value: Any?) {
        when (property) {
            COMPLETER -> completionHelper.completer = completer
        }
    }

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
            registerShortcuts {
                on("Ctrl + Space") { completionHelper.show(this@FXExpanderView) }
                on("Enter") { expander.expand() }
            }
            textSubscription = userUpdatedText.subscribe { new -> expander.setText(new) }
        }
        expander.editor.now?.let { showContent(it) }
        expander.text.now?.let { displayText(it) }
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

    final override fun expanded(editor: Editor<*>) {
        val v = showContent(editor)
        onExpansion(editor, v)
    }

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
        val COMPLETER = Property<Completer<String>, Public, Public>("completer")
    }
}