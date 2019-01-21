/**
 *@author Nikolaus Knop
 */

package hextant.sample.editable

import hextant.Editable
import hextant.base.AbstractEditable
import hextant.sample.ast.IntExpr
import hextant.sample.ast.IntOperatorApplication
import reaktive.value.ReactiveValue
import reaktive.value.binding.flatMap
import reaktive.value.binding.map
import reaktive.value.reactiveValue

class EditableIntOperatorApplication : AbstractEditable<IntOperatorApplication>() {
    val left: Editable<IntExpr> = ExpandableIntExpr()
    val op = EditableIntOperator()
    val right: Editable<IntExpr> = ExpandableIntExpr()

    override val edited: ReactiveValue<IntOperatorApplication?> = left.edited.flatMap { l ->
        if (l == null) reactiveValue(null)
        else op.edited.flatMap { o ->
            if (o == null) reactiveValue(null)
            else right.edited.map { r ->
                if (r == null) null
                else IntOperatorApplication(l, o, r)
            }
        }
    }
}