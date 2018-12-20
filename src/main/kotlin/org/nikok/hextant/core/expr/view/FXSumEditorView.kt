/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.view

import org.nikok.hextant.Context
import org.nikok.hextant.core.base.CompoundEditorControl
import org.nikok.hextant.core.expr.editable.EditableSum
import org.nikok.hextant.core.expr.editor.SumEditor
import org.nikok.hextant.getEditor

class FXSumEditorView(
    editable: EditableSum,
    context: Context
) : CompoundEditorControl(context, {
    line {
        val editor: SumEditor = context.getEditor(editable) as SumEditor
        keyword("sum").setOnMouseClicked {
            editor.select()
        }
        operator("(")
    }
    indented {
        view(editable.expressions)
    }
    operator(")")
}) {
    init {
        val editor: SumEditor = context.getEditor(editable) as SumEditor
        editor.addView(this)
        initialize(editable, editor, context)
    }
}