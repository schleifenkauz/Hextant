/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import hextant.base.EditorControl
import hextant.bundle.*
import hextant.bundle.CorePermissions.Public
import hextant.completion.Completer
import hextant.completion.NoCompleter
import hextant.completion.gui.CompleterPopupHelper
import hextant.core.editor.TokenEditor
import hextant.fx.*
import hextant.impl.subscribe
import hextant.main.InputMethod
import javafx.scene.input.KeyCode.SPACE
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import java.lang.ref.WeakReference

/**
 * An [EditorControl] for [TokenEditor]'s.
 * The constructor of this class doesn't register itself as a view of the supplied editor.
 * Most likely you should use [FXTokenEditorView] instead.
 */
open class AbstractTokenEditorControl(
    private val editor: TokenEditor<*, *>,
    args: Bundle,
    completer: Completer<String> = NoCompleter
) : EditorControl<HextantTextField>(editor, args), TokenEditorView {
    private val textField = HextantTextField(initialInputMethod = context[Public, InputMethod])

    init {
        registerShortcut()
    }

    private fun registerShortcut() {
        val ref = WeakReference(this)
        registerShortcut(KeyCodeCombination(SPACE, KeyCombination.CONTROL_DOWN)) {
            ref.get()?.completionHelper?.show(ref.get()!!)
        }
    }

    private val textSubscription = textField.userUpdatedText.subscribe(this) { _, new ->
        editor.setText(new)
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
            COMPLETER -> completionHelper.completer = completer
        }
    }

    private val completionHelper = CompleterPopupHelper(completer, this.textField::getText)

    private val obs = completionHelper.completionChosen.subscribe(this) { _, c ->
        editor.setText(c.completed)
        scene.focusNext()
    }

    final override fun createDefaultRoot() = textField

    final override fun displayText(newText: String) {
        root.smartSetText(newText)
        completionHelper.show(this)
    }

    override fun receiveFocus() {
        textField.requestFocus()
    }

    companion object {
        /**
         * This property controls the completer of the token editor control
         */
        val COMPLETER = Property<Completer<String>, Public, Public>("completer")
    }
}