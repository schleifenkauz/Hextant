/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editor

import org.nikok.hextant.EditorView
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.base.AbstractEditor
import org.nikok.hextant.core.expr.editable.EditableSum
import org.nikok.hextant.core.expr.edited.Expr
import org.nikok.reaktive.value.now

class SumEditor(
    sum: EditableSum,
    platform: HextantPlatform
) : ExprEditor,
    AbstractEditor<EditableSum, EditorView>(sum, platform) {
    override val expr: Expr?
        get() = editable.edited.now
}