/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import hextant.Context
import hextant.base.CompoundEditorControl
import hextant.bundle.Bundle
import hextant.sample.editor.IntOperatorApplicationEditor

class FXIntOperatorApplicationEditorView(
    editor: IntOperatorApplicationEditor,
    context: Context,
    args: Bundle
) :
    CompoundEditorControl(editor, context, args, {
        line {
            view(editor.left)
            view(editor.op)
            view(editor.right)
        }
    })