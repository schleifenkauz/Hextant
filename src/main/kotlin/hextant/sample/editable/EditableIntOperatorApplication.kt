/**
 *@author Nikolaus Knop
 */

package hextant.sample.editable

import hextant.Editable
import hextant.base.AbstractEditable
import hextant.sample.ast.IntExpr
import hextant.sample.ast.IntOperatorApplication
import org.nikok.reaktive.value.ReactiveValue
import org.nikok.reaktive.value.reactiveValue

class EditableIntOperatorApplication : AbstractEditable<IntOperatorApplication>() {
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
}