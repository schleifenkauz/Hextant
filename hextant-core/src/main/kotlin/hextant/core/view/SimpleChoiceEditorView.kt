package hextant.core.view

import hextant.core.EditorView

interface SimpleChoiceEditorView<C> : EditorView {
    fun selected(choice: C)
}