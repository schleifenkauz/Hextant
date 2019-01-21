/**
 * @author Nikolaus Knop
 */

package hextant.sample.editable

import hextant.sample.ast.IntExpr
import reaktive.value.ReactiveValue

interface EditableIntExpr {
    val expr: ReactiveValue<IntExpr?>
}