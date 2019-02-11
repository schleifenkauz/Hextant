/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import hextant.Context
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.completion.Completion
import hextant.completion.gui.CompletionPopup
import hextant.core.editable.EditableToken
import hextant.core.editor.TokenEditor
import hextant.fx.HextantTextField
import hextant.fx.smartSetText
import hextant.getEditor
import reaktive.event.Subscription

open class FXTokenEditorView(
    editable: EditableToken<Any>,
    context: Context,
    args: Bundle
) : EditorControl<HextantTextField>(args), TokenEditorView {
    private val completionPopup = CompletionPopup<String>()

    private val textSubscription: Subscription

    private val textField = HextantTextField()

    final override fun createDefaultRoot() = HextantTextField()

    @Suppress("UNCHECKED_CAST")
    private val editor = context.getEditor(editable) as TokenEditor<*, TokenEditorView>

    init {
        initialize(editable, editor, context)
        with(textField) {
            textSubscription = userUpdatedText.subscribe { _, new ->
                editor.setText(new)
            }
        }
        editor.addView(this)
    }

    final override fun displayText(t: String) {
        root.smartSetText(t)
    }


    override fun displayCompletions(completions: Collection<Completion<String>>) {
        completionPopup.setCompletions(completions)
    }
}