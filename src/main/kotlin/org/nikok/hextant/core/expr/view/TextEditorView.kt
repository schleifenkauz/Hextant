package org.nikok.hextant.core.view

import org.nikok.hextant.EditorView

interface TextEditorView: EditorView {
    fun displayText(t: String)
}
