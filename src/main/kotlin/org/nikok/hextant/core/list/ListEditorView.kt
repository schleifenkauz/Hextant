package org.nikok.hextant.core.list

import org.nikok.hextant.Editable
import org.nikok.hextant.EditorView

interface ListEditorView: EditorView {
    fun added(editable: Editable<*>, idx: Int)

    fun removed(idx: Int)

    fun empty()

    fun notEmpty()
}
