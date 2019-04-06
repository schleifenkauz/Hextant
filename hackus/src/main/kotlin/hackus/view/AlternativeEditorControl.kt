/**
 *@author Nikolaus Knop
 */

package hackus.view

import hackus.editable.EditableAlternative
import hextant.*
import hextant.base.CompoundEditorControl
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Public
import hextant.core.list.FXListEditorView
import hextant.core.list.FXListEditorView.Orientation

class AlternativeEditorControl(
    editable: EditableAlternative,
    context: Context,
    args: Bundle
) : CompoundEditorControl(editable, context, args, {
        line {
            keyword("alt")
        }
        indented {
            view(editable.alternatives, Bundle.configure {
                set(Public, FXListEditorView.ORIENTATION, Orientation.Vertical)
            })
        }
    })