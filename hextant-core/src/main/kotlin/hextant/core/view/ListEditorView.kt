package hextant.core.view

import hextant.Editor
import hextant.EditorView

interface ListEditorView: EditorView {
    fun added(editable: Editor<*>, idx: Int)

    fun removed(idx: Int)

    fun empty()

    fun notEmpty()
}
