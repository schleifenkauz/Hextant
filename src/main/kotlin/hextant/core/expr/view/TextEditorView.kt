package hextant.core.expr.view

import hextant.EditorView

interface TextEditorView: EditorView {
    fun displayText(t: String)
}
