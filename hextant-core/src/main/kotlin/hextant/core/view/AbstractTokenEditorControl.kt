/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.completion.Completer
import hextant.completion.NoCompleter
import hextant.completion.gui.CompleterPopupHelper
import hextant.core.editor.TokenEditor
import hextant.fx.HextantTextField
import hextant.fx.smartSetText
import reaktive.event.subscribe

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
    private val textField = HextantTextField()

    private val textSubscription = textField.userUpdatedText.subscribe { new ->
        editor.setText(new)
    }

    private val completionHelper = CompleterPopupHelper(completer, this.textField::getText)

    final override fun createDefaultRoot() = textField

    final override fun displayText(newText: String) {
        root.smartSetText(newText)
        completionHelper
    }

    override fun receiveFocus() {
        textField.requestFocus()
    }
}