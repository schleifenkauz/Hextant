/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import bundles.publicProperty
import bundles.set
import hextant.codegen.ProvideImplementation
import hextant.completion.Completer
import hextant.completion.NoCompleter
import hextant.completion.gui.CompletionPopup
import hextant.context.ControlFactory
import hextant.context.createControl
import hextant.core.Editor
import hextant.core.editor.Expander
import hextant.fx.*
import javafx.scene.Node
import reaktive.Observer
import reaktive.value.now

/**
 * JavaFX implementation of a [ExpanderView]
 */
open class ExpanderControl @ProvideImplementation(ControlFactory::class) constructor(
    private val expander: Expander<*, *>,
    args: Bundle
) : ExpanderView, WrappingEditorControl<Node>(expander, args) {
    constructor(expander: Expander<*, *>, args: Bundle, completer: Completer<Expander<*, *>, Any>) :
            this(expander, args.also { it[COMPLETER] = completer })


    private val textField = HextantTextField(initialInputMethod = context[InputMethod])

    private val popup = CompletionPopup(context, expander) { arguments[COMPLETER] }

    private val textObserver: Observer
    private val completionObserver: Observer
    private val styleObserver: Observer
    private val textEmptyObserver: Observer

    override fun createDefaultRoot(): Node = textField

    init {
        with(textField) {
            registerShortcuts {
                on("Ctrl + Space") { popup.show(root) }
                on("Enter") { expander.expand() }
            }
            textObserver = userUpdatedText.observe { _, new ->
                expander.setText(new)
                popup.updateInput(new)
                popup.show(root)
            }
            if (expander.text.now == "") styleClass.add("empty-text")
            textEmptyObserver = expander.text.observe { _, old, new ->
                if (new == "") styleClass.add("empty-text")
                else if (old == "") styleClass.remove("empty-text")
            }
        }
        styleClass.add("expander")
        completionObserver = popup.completionChosen.observe { _, completion ->
            if (!expander.isExpanded.now) expander.complete(completion)
        }
        styleObserver = context[ResultStyleClasses].apply(this)
        expander.addView(this)
    }

    override fun displayText(text: String) {
        if (text != textField.text) {
            textField.text = text
            popup.show(root)
        }
    }

    override fun receiveFocus() {
        if (wrapped != null) wrapped!!.receiveFocus()
        else textField.requestFocus()
    }

    override fun reset() {
        wrapped = null
        root = textField
        textField.text = ""
        runFXWithTimeout { textField.requestFocus() }
    }

    final override fun expanded(editor: Editor<*>) {
        if (root is EditorControl<*>) removeChild(0)
        val v = context.createControl(editor)
        v.registerShortcuts { on("Ctrl? + R") { expander.reset() } }
        wrapped = v
        root = v
        v.receiveFocus()
        onExpansion(editor, v)
    }

    /**
     * Called when the [Expander] has been expanded to the given [editor].
     * The default implementation does nothing.
     */
    protected open fun onExpansion(editor: Editor<*>, control: EditorControl<*>) {}

    companion object {
        /**
         * This property controls the completer of the expander control
         */
        val COMPLETER = publicProperty<Completer<Expander<*, *>, Any>>("expander.completer", default = NoCompleter)
    }
}