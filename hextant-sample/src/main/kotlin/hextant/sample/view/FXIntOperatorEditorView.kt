/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import hextant.Context
import hextant.core.editable.EditableToken
import hextant.expr.view.FXTokenEditorView

class FXIntOperatorEditorView(
    editable: EditableToken<Any>,
    context: Context
) : FXTokenEditorView(editable, context) {
    init {
        styleClass.add("int-operator-editor")
    }
}