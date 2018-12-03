/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.editable

import org.nikok.hextant.Editable
import org.nikok.hextant.sample.ast.IntExpr
import org.nikok.hextant.sample.ast.IntOperatorApplication
import org.nikok.reaktive.value.*
import org.nikok.reaktive.value.binding.impl.notNull

class EditableIntOperatorApplication : Editable<IntOperatorApplication> {
    val left: Editable<IntExpr> = TODO()
    val op: EditableIntOperator = EditableIntOperator()
    val right: Editable<IntExpr> = TODO()

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
    override val isOk: ReactiveBoolean = edited.notNull()
}