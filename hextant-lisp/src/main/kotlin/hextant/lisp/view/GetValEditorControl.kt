/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import hextant.*
import hextant.base.EditorControl
import hextant.lisp.editable.EditableGetVal
import javafx.scene.Node

class GetValEditorControl(
    private val editable: EditableGetVal,
    private val context: Context
) : EditorControl<Node>() {
    init {
        val editor = context.getEditor(editable)
        initialize(editable, editor, context)
    }

    override fun createDefaultRoot(): Node = context.createView(editable.searchedIdentifier).root
}