/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editor

import org.nikok.hextant.Context
import org.nikok.hextant.EditorView
import org.nikok.hextant.core.base.AbstractEditor
import org.nikok.hextant.core.expr.editable.EditableSum
import org.nikok.hextant.core.expr.edited.Expr
import org.nikok.reaktive.value.now

class SumEditor(
    sum: EditableSum,
    context: Context
) : ExprEditor,
    AbstractEditor<EditableSum, EditorView>(sum, context) {
    override val expr: Expr?
        get() = editable.edited.now
}