/**
 *@author Nikolaus Knop
 */

package hextant.sample.editable

import hextant.*
import hextant.base.AbstractEditable
import hextant.sample.ast.IntExpr
import hextant.sample.ast.IntOperatorApplication
import reaktive.dependencies
import reaktive.value.binding.binding
import reaktive.value.now

class EditableIntOperatorApplication : AbstractEditable<IntOperatorApplication>() {
    val left: Editable<IntExpr> = ExpandableIntExpr()
    val op = EditableIntOperator()
    val right: Editable<IntExpr> = ExpandableIntExpr()

    override val result: RResult<IntOperatorApplication> =
        binding<CompileResult<IntOperatorApplication>>(dependencies(left.result, op.result, right.result)) {
            compile {
                Ok(IntOperatorApplication(left.result.now.force(), op.result.now.force(), right.result.now.force()))
            }
        }
}