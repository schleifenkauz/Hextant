/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import hextant.Context
import hextant.base.CompoundEditorControl
import hextant.sample.editable.EditableIntOperatorApplication

class FXIntOperatorApplicationEditorView(editable: EditableIntOperatorApplication, context: Context) :
    CompoundEditorControl(context, {
        line {
            view(editable.left)
            view(editable.op)
            view(editable.right)
        }
    },)