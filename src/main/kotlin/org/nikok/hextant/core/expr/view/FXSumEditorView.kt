/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.view

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.base.CompoundEditorControl
import org.nikok.hextant.core.expr.editable.EditableSum
import org.nikok.hextant.core.expr.editor.SumEditor

class FXSumEditorView(
    editableSum: EditableSum,
    platform: HextantPlatform
) : CompoundEditorControl(platform, {
    line {
        keyword("sum")
        operator("(")
    }
    indented {
        view(editableSum.expressions)
    }
    operator(")")
}) {
    init {
        val editor = SumEditor(editableSum, platform)
        initialize(editableSum, editor, platform)
    }
}