/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package org.nikok.hextant.core.expr.editable

import org.nikok.hextant.Editable
import org.nikok.hextant.ParentEditable
import org.nikok.hextant.core.expr.edited.Expr
import org.nikok.hextant.core.expr.edited.Sum
import org.nikok.reaktive.value.ReactiveValue
import org.nikok.reaktive.value.now

class EditableSum : ParentEditable<Sum, Editable<Expr>>(), EditableExpr<Sum> {
    override fun accepts(child: Editable<*>): Boolean = child is EditableExprList

    val expressions = EditableExprList()

    init {
        expressions.moveTo(this)
    }

    override val edited: ReactiveValue<Sum?>
        get() = expressions.edited.map("edited of sum") { editableExpressions ->
            editableExpressions.map { editable ->
                editable?.edited?.now
            }.takeIf { expressions ->
                expressions.all { expr -> expr != null }
            }?.let { Sum(it as List<Expr>) }
        }
}