/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import hextant.Context
import hextant.base.CompoundEditorControl
import hextant.bundle.Bundle
import hextant.sample.editable.EditableIntOperatorApplication

class FXIntOperatorApplicationEditorView(
    editable: EditableIntOperatorApplication,
    context: Context,
    args: Bundle
) :
    CompoundEditorControl(editable, context, args, {
        line {
            view(editable.left)
            view(editable.op)
            view(editable.right)
        }
    })