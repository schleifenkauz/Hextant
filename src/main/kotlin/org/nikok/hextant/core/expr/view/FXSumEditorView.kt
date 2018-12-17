/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.view

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.base.CompoundEditorControl
import org.nikok.hextant.core.expr.editable.EditableSum

class FXSumEditorView(
    editableSum: EditableSum,
    platform: HextantPlatform
) : CompoundEditorControl(platform, {
    keyword("sum")
    indented {
        view(editableSum.expressions)
    }
})