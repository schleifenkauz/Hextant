/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.view

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.EditorFactory
import org.nikok.hextant.core.base.CompoundEditorControl
import org.nikok.hextant.core.expr.editable.EditableSum
import org.nikok.hextant.core.expr.editor.SumEditor
import org.nikok.hextant.core.getEditor

class FXSumEditorView(
    editable: EditableSum,
    platform: HextantPlatform
) : CompoundEditorControl(platform, {
    line {
        val editor: SumEditor = platform[Public, EditorFactory].getEditor(editable)
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
        val editor: SumEditor = platform[Public, EditorFactory].getEditor(editable)
        editor.addView(this)
        initialize(editable, editor, platform)
    }
}