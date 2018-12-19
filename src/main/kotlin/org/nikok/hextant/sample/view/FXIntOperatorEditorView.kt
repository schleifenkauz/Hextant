/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.view

import org.nikok.hextant.Context
import org.nikok.hextant.core.editable.EditableToken
import org.nikok.hextant.core.expr.view.FXTokenEditorView

class FXIntOperatorEditorView(
    editable: EditableToken<Any>,
    context: Context
) : FXTokenEditorView(editable, context) {
    init {
        styleClass.add("int-operator-editor")
    }
}