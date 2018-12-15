/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.editable

import org.nikok.hextant.Editable
import org.nikok.hextant.core.editable.Expandable
import org.nikok.hextant.sample.ast.IntExpr
import org.nikok.reaktive.value.ReactiveValue

class ExpandableIntExpr : Expandable<IntExpr, Editable<IntExpr>>(), EditableIntExpr {
    override fun accepts(child: Editable<*>): Boolean = child is EditableIntExpr

    override val expr: ReactiveValue<IntExpr?>
        get() = edited
}