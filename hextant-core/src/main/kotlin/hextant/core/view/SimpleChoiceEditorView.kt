package hextant.core.view

import hextant.core.EditorView

interface SimpleChoiceEditorView<C : Any> : EditorView {
    fun selected(choice: C)
}