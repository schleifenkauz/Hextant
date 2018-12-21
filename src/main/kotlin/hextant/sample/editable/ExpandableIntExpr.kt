/**
 *@author Nikolaus Knop
 */

package hextant.sample.editable

import hextant.Editable
import hextant.core.editable.Expandable
import hextant.sample.ast.IntExpr
import org.nikok.reaktive.value.ReactiveValue

class ExpandableIntExpr : Expandable<IntExpr, Editable<IntExpr>>(), EditableIntExpr {
    override fun accepts(child: Editable<*>): Boolean = child is EditableIntExpr

    override val expr: ReactiveValue<IntExpr?>
        get() = edited
}