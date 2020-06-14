/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.*
import hextant.Context
import hextant.completion.Completer
import hextant.completion.NoCompleter
import hextant.completion.gui.CompletionPopup
import hextant.core.editor.TokenEditor
import hextant.fx.*
import hextant.impl.observe
import hextant.main.InputMethod
import javafx.scene.input.KeyCode.SPACE
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination

/**
 * An [EditorControl] for [TokenEditor]'s.
 * The constructor of this class doesn't register itself as a view of the supplied editor.
 * Most likely you should use [FXTokenEditorView] instead.
 */
open class AbstractTokenEditorControl(
    private val editor: TokenEditor<*, *>,
    args: Bundle
) : EditorControl<HextantTextField>(editor, args), TokenEditorView {
    private val textField = HextantTextField(initialInputMethod = context[InputMethod])

    /**
     * The completer used by this TokenEditorControl
     */
    var completer
        get() = arguments[COMPLETER]
        set(value) {
            arguments[COMPLETER] = value
        }

    init {
        registerShortcut()
    }

    private fun registerShortcut() {
        registerShortcut(KeyCodeCombination(SPACE, KeyCombination.CONTROL_DOWN)) {
            popup.show(this)
        }
    }

    private val textObserver = textField.userUpdatedText.observe(this) { _, new ->
        editor.setText(new)
        popup.updateInput(new)
        popup.show(this)
    }

    override fun argumentChanged(property: Property<*, *, *>, value: Any?) {
        when (property) {
            COMPLETER -> popup.completer = completer
        }
    }

    private val popup = CompletionPopup(context, context[IconManager], this.completer)

    private val obs = popup.completionChosen.observe(this) { _, c ->
        editor.complete(c)
        scene.selectNext()
    }

    final override fun createDefaultRoot() = textField

    final override fun displayText(newText: String) {
        root.smartSetText(newText)
        popup.show(this)
    }

    override fun receiveFocus() {
        textField.requestFocus()
    }

    companion object {
        /**
         * This property controls the completer of the token editor control
         */
        val COMPLETER = SimpleProperty<Completer<Context, Any>>("completer", default = NoCompleter)
    }
}