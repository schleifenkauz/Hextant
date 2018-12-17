/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package org.nikok.hextant.core.expr.editable

import org.nikok.hextant.Editable
import org.nikok.hextant.ParentEditable
import org.nikok.hextant.core.expr.edited.Expr
import org.nikok.hextant.core.expr.edited.Sum
import org.nikok.hextant.core.list.EditableList
import org.nikok.reaktive.value.ReactiveValue
import org.nikok.reaktive.value.now

class EditableSum : ParentEditable<Sum, Editable<Expr>>(), EditableExpr<Sum> {
    override fun accepts(child: Editable<*>): Boolean = child is EditableExpr<*>

    val expressions = EditableList.newInstance<Expr, Editable<Expr>>()

    override val edited: ReactiveValue<Sum?>
        get() = expressions.edited.map("edited of sum") { editableExpressions ->
            editableExpressions.map { editable ->
                editable?.edited?.now
            }.takeIf { expressions ->
                expressions.all { expr -> expr != null }
            }?.let { Sum(it as List<Expr>) }
        }
}