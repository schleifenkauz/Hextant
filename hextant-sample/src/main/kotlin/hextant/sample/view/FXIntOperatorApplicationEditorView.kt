/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import bundles.Bundle
import hextant.core.view.CompoundEditorControl
import hextant.sample.editor.IntOperatorApplicationEditor

class FXIntOperatorApplicationEditorView(
    editor: IntOperatorApplicationEditor,
    args: Bundle
) : CompoundEditorControl(editor, args, {
    line {
        view(editor.left)
        view(editor.op)
        view(editor.right)
    }
})