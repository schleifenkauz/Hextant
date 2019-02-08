/**
 *@author Nikolaus Knop
 */

package hextant.expr.view

import hextant.Context
import hextant.bundle.Bundle
import hextant.core.view.FXTokenEditorView
import hextant.expr.editable.EditableOperator

class FXOperatorEditorView(
    editable: EditableOperator,
    context: Context,
    args: Bundle
) :
    FXTokenEditorView(editable, context, args)