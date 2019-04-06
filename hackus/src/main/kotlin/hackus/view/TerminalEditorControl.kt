/**
 * @author Nikolaus Knop
 */

package hackus.view

import hackus.editable.EditableTerminal
import hextant.Context
import hextant.base.CompoundEditorControl
import hextant.bundle.Bundle

class TerminalEditorControl(
    editable: EditableTerminal,
    context: Context,
    args: Bundle
) :
    CompoundEditorControl(editable, context, args, {
        line {
            keyword("token")
            view(editable.fqName)
        }
    })