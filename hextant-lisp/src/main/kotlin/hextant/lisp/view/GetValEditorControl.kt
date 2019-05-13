/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.createView
import hextant.lisp.editor.GetValEditor
import javafx.scene.Node

class GetValEditorControl(
    private val editor: GetValEditor,
    args: Bundle
) : EditorControl<Node>(editor, args) {
    override fun receiveFocus() {
        root.requestFocus()
    }

    init {
        editor.addView(this)
    }

    override fun createDefaultRoot(): Node = context.createView(editor.searchedIdentifier).root
}