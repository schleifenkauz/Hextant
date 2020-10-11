/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import bundles.SimpleProperty
import hextant.codegen.ProvideImplementation
import hextant.completion.Completer
import hextant.completion.NoCompleter
import hextant.completion.gui.CompletionPopup
import hextant.context.Context
import hextant.context.ControlFactory
import hextant.core.editor.ValidatedTokenEditor
import hextant.fx.*

/**
 * Displays a [ValidatedTokenEditor] as a [HextantTextField].
 */
open class ValidatedTokenEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    private val editor: ValidatedTokenEditor<*>, arguments: Bundle
) :
    EditorControl<HextantTextField>(editor, arguments), ValidatedTokenEditorView {
    private val popup = CompletionPopup.forContext(context) { arguments[COMPLETER] }

    private val textObserver = root.userUpdatedText.observe { _, new ->
        editor.setText(new)
        popup.updateInput(new)
        popup.show(root)
    }

    private val completer = popup.completionChosen.observe { _, completion -> editor.complete(completion) }

    init {
        listenForCommands()
        editor.addView(this)
    }

    override fun displayText(text: String) {
        root.smartSetText(text)
    }

    private fun listenForCommands() {
        registerShortcuts {
            on(arguments[COMMIT_CHANGE]) { editor.commitChange() }
            on(arguments[BEGIN_CHANGE]) { editor.beginChange() }
            on(arguments[ABORT_CHANGE]) { editor.abortChange() }
            on("Ctrl+Space") { popup.show(root) }
        }
    }

    override fun setEditable(editable: Boolean) {
        root.isEditable = editable
        if (editable) {
            root.requestFocus()
            root.selectAll()
        }
    }

    override fun createDefaultRoot(): HextantTextField = HextantTextField()

    companion object {
        /**
         * Keyboard shortcut for the commit change action.
         */
        val COMMIT_CHANGE = SimpleProperty.withDefault("commit", never())

        /**
         * Keyboard shortcut for the begin change action.
         */
        val BEGIN_CHANGE = SimpleProperty.withDefault("begin change", never())

        /**
         * Keyboard shortcut for the abort change action.
         */
        val ABORT_CHANGE = SimpleProperty.withDefault("abort change", never())

        /**
         * Completer used by the [ValidatedTokenEditorControl]
         */
        val COMPLETER = SimpleProperty.withDefault<Completer<Context, *>>("completer", NoCompleter)
    }
}