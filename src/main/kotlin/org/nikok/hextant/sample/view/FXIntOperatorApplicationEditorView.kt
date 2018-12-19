/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.view

import org.nikok.hextant.Context
import org.nikok.hextant.core.base.CompoundEditorControl
import org.nikok.hextant.sample.editable.EditableIntOperatorApplication

class FXIntOperatorApplicationEditorView(editable: EditableIntOperatorApplication, context: Context) :
    CompoundEditorControl(context, {
        line {
            view(editable.left)
            view(editable.op)
            view(editable.right)
        }
    })