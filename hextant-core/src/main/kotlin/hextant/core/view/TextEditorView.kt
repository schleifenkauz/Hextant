package hextant.core.view

import hextant.EditorView

interface TextEditorView: EditorView {
    fun displayText(t: String)
}
