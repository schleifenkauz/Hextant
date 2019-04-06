/**
 *@author Nikolaus Knop
 */

package hackus.view

import hackus.editable.EditableDefinition
import hextant.Context
import hextant.base.CompoundEditorControl
import hextant.bundle.Bundle

class DefinitionEditorControl(
    editable: EditableDefinition,
    context: Context,
    args: Bundle
) : CompoundEditorControl(editable, context, args, {
    line {
        view(editable.name)
        operator(":=")
        view(editable.rightSide)
    }
})