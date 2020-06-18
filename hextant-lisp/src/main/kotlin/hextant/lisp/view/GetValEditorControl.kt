/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import bundles.Bundle
import hextant.context.createView
import hextant.fx.EditorControl
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