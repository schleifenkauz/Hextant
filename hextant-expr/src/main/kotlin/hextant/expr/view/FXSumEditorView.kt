/**
 *@author Nikolaus Knop
 */

package hextant.expr.view

import hextant.Context
import hextant.base.CompoundEditorControl
import hextant.expr.editable.EditableSum
import hextant.expr.editor.SumEditor
import hextant.getEditor

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