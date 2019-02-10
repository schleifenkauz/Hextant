package hextant.core.view

import hextant.EditorView
import hextant.completion.Completion

interface TextEditorView: EditorView {
    fun displayText(t: String)

    fun displayCompletions(completions: Collection<Completion<String>>)
}
