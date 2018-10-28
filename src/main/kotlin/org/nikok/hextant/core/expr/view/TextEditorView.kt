package org.nikok.hextant.core.expr.view

import org.nikok.hextant.EditorView

interface TextEditorView: EditorView {
    fun displayText(t: String)
}
