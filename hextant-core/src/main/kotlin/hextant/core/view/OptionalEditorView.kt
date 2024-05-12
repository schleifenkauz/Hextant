package hextant.core.view

import hextant.core.Editor
import hextant.core.EditorView

interface OptionalEditorView : EditorView {
    fun removed()

    fun display(content: Editor<*>)
}