/**
 *@author Nikolaus Knop
 */

package hextant.sample.editable

import hextant.Editable
import hextant.core.editable.Expandable
import hextant.sample.ast.IntExpr
import reaktive.value.ReactiveValue

class ExpandableIntExpr : Expandable<IntExpr, Editable<IntExpr>>(), EditableIntExpr {
    override val expr: ReactiveValue<IntExpr?>
        get() = edited
}