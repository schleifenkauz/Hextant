/**
 *@author Nikolaus Knop
 */

package hextant.expr.view

import hextant.base.CompoundEditorControl
import hextant.bundle.Bundle
import hextant.expr.editor.SumEditor

class FXSumEditorView(
    editor: SumEditor,
    args: Bundle
) : CompoundEditorControl(editor, args, {
    line {
        keyword("sum")
    }
    indented {
        view(editor.expressions)
    }
})