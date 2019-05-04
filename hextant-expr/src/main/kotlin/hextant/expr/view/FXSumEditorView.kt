/**
 *@author Nikolaus Knop
 */

package hextant.expr.view

import hextant.Context
import hextant.base.CompoundEditorControl
import hextant.bundle.Bundle
import hextant.expr.editor.SumEditor

class FXSumEditorView(
    editor: SumEditor,
    context: Context,
    args: Bundle
) : CompoundEditorControl(editor, context, args, {
    line {
        keyword("sum")
        operator("(")
    }
    indented {
        view(editor.expressions)
    }
    operator(")")
})