/**
 *@author Nikolaus Knop
 */

package hextant.sample.editable

import hextant.Editable
import hextant.core.editable.Expandable
import hextant.orNull
import hextant.sample.ast.IntExpr
import reaktive.value.ReactiveValue
import reaktive.value.binding.map

class ExpandableIntExpr : Expandable<IntExpr, Editable<IntExpr>>(),
                          EditableIntExpr {
    override val expr: ReactiveValue<IntExpr?>
        get() = result.map { it.orNull() }
}