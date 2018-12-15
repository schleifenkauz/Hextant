/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.editable

import org.nikok.hextant.Editable
import org.nikok.hextant.ParentEditable
import org.nikok.hextant.sample.ast.IntExpr
import org.nikok.hextant.sample.ast.IntOperatorApplication
import org.nikok.reaktive.value.ReactiveValue
import org.nikok.reaktive.value.reactiveValue

class EditableIntOperatorApplication : ParentEditable<IntOperatorApplication, Editable<*>>() {
    val left: Editable<IntExpr> = ExpandableIntExpr()
    val op = EditableIntOperator()
    val right: Editable<IntExpr> = ExpandableIntExpr()

    override val edited: ReactiveValue<IntOperatorApplication?> = left.edited.flatMap("map left") { l ->
        if (l == null) reactiveValue("edited", null)
        else op.edited.flatMap("map op") { o ->
            if (o == null) reactiveValue("edited", null)
            else right.edited.map("map right") { r ->
                if (r == null) null
                else IntOperatorApplication(l, o, r)
            }
        }
    }

    override fun accepts(child: Editable<*>): Boolean {
        return child is EditableIntOperator || child is EditableIntExpr
    }
}