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
import reaktive.event.Subscription
import reaktive.event.subscribe

open class FXTokenEditorView(
    private val editor: TokenEditor<*>,
    args: Bundle,
    completer: Completer<String> = NoCompleter
) : EditorControl<HextantTextField>(editor, args), TokenEditorView {
    private val textField = HextantTextField()

    private val textSubscription: Subscription

    private val completionHelper = CompleterPopupHelper(completer, this.textField::getText)

    final override fun createDefaultRoot() = textField

    init {
        with(textField) {
            textSubscription = userUpdatedText.subscribe { new ->
                this@FXTokenEditorView.editor.setText(new)
            }
        }
        this.editor.addView(this)
    }

    final override fun displayText(newText: String) {
        root.smartSetText(newText)
        completionHelper
    }

    override fun receiveFocus() {
        textField.requestFocus()
    }
}