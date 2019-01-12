package hextant.core.list

import hextant.Editable
import hextant.EditorView

interface ListEditorView: EditorView {
    fun added(editable: Editable<*>, idx: Int)

    fun removed(idx: Int)

    fun empty()

    fun notEmpty()
}
