/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.sample.ast.IntOperatorApplication
import reaktive.dependencies
import reaktive.value.binding.binding
import reaktive.value.now

class IntOperatorApplicationEditor(context: Context) : AbstractEditor<IntOperatorApplication, EditorView>(context) {
    val left = IntExprExpander(context)
    val op = IntOperatorEditor(context)
    val right = IntExprExpander(context)

    override val result: EditorResult<IntOperatorApplication> =
        binding<CompileResult<IntOperatorApplication>>(dependencies(left.result, op.result, right.result)) {
            compile {
                Ok(IntOperatorApplication(left.result.now.force(), op.result.now.force(), right.result.now.force()))
            }
        }
}