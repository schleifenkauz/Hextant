package hextant.blocky.view

import hextant.Editor

interface ProgramEditorView {
    fun addedComponent(idx: Int, comp: Editor<*>)

    fun removedComponent(comp: Editor<*>)
}
