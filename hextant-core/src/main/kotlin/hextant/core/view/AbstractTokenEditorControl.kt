/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import hextant.Context
import hextant.base.EditorControl
import hextant.bundle.*
import hextant.bundle.CorePermissions.Public
import hextant.completion.Completer
import hextant.completion.NoCompleter
import hextant.completion.gui.CompletionPopup
import hextant.core.editor.TokenEditor
import hextant.fx.*
import hextant.get
import hextant.impl.subscribe
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
    args: Bundle,
    completer: Completer<Context, String> = NoCompleter
) : EditorControl<HextantTextField>(editor, args), TokenEditorView {
    private val textField = HextantTextField(initialInputMethod = context[Public, InputMethod])

    init {
        registerShortcut()
    }

    private fun registerShortcut() {
        registerShortcut(KeyCodeCombination(SPACE, KeyCombination.CONTROL_DOWN)) {
            popup.show(this)
        }
    }

    private val textSubscription = textField.userUpdatedText.subscribe(this) { _, new ->
        editor.setText(new)
        popup.updateInput(new)
        popup.show(this)
    }

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
            COMPLETER -> popup.completer = completer
        }
    }

    private val popup = CompletionPopup(context, context[IconManager], completer)

    private val obs = popup.completionChosen.subscribe(this) { _, c ->
        editor.setText(c.completion)
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
        val COMPLETER = Property<Completer<Context, String>, Public, Public>("completer")
    }
}