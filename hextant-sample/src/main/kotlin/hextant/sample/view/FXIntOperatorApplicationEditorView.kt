/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import hextant.base.CompoundEditorControl
import hextant.bundle.Bundle
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