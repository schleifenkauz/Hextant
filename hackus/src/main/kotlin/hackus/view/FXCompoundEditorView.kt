/**
 *@author Nikolaus Knop
 */

package hackus.view

import hackus.editable.EditableCompound
import hextant.Context
import hextant.base.CompoundEditorControl
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Public
import hextant.core.list.FXListEditorView
import hextant.core.list.FXListEditorView.Orientation

class FXCompoundEditorView(editable: EditableCompound, context: Context, args: Bundle) :
    CompoundEditorControl(editable, context, args, {
        line {
            keyword("compound")
        }
        line {
            indented {
                view(editable.subNodes, Bundle.configure {
                    set(Public, FXListEditorView.ORIENTATION, Orientation.Vertical)
                })
            }
        }
    })