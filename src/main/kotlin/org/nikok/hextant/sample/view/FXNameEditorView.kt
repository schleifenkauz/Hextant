/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.view

import org.nikok.hextant.Context
import org.nikok.hextant.core.expr.view.FXTokenEditorView
import org.nikok.hextant.sample.editable.EditableName

class FXNameEditorView(
    editable: EditableName,
    context: Context
) : FXTokenEditorView(editable, context) {
    init {
        styleClass.add("identifier-editor")
    }
}