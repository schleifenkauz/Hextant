/**
 * @author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.sample.ast.IntExpr
import reaktive.value.ReactiveValue

interface IntExprEditor {
    val expr: ReactiveValue<IntExpr?>
}