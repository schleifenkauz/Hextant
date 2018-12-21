/**
 * @author Nikolaus Knop
 */

package hextant.sample.editable

import hextant.sample.ast.IntExpr
import org.nikok.reaktive.value.ReactiveValue

interface EditableIntExpr {
    val expr: ReactiveValue<IntExpr?>
}