/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import bundles.SimpleProperty
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
 * Most likely you should use [TokenEditorControl] instead.
 */
open class TokenEditorControl(editor: TokenEditor<*>, args: Bundle) : EditorControl<HextantTextField>(editor, args),
                                                                      TokenEditorView {
    private val textField = HextantTextField(initialInputMethod = context[InputMethod])
    private val textObserver = textField.userUpdatedText.observe(this) { _, new ->
        editor.setText(new)
        popup.updateInput(new)
        popup.show(this)
    }

    private val popup = CompletionPopup(context, context[IconManager], context[COMPLETER])

    private val obs = popup.completionChosen.observe(this) { _, c ->
        editor.complete(c)
        scene.selectNext()
    }

    init {
        registerShortcut()
        editor.addView(this)
    }

    private fun registerShortcut() {
        registerShortcut(KeyCodeCombination(SPACE, KeyCombination.CONTROL_DOWN)) {
            popup.show(this)
        }
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