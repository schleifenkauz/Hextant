/**
 *@author Nikolaus Knop
 */

package hackus.view

import hackus.editable.EditableSubNode
import hextant.Context
import hextant.base.CompoundEditorControl
import hextant.bundle.Bundle

class SubNodeEditorControl(
    editable: EditableSubNode,
    context: Context,
    args: Bundle
) : CompoundEditorControl(editable, context, args, {
    line {
        view(editable.name)
        operator(":")
        view(editable.type)
    }
})