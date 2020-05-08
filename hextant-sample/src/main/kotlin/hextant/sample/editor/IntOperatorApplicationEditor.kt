/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.*
import hextant.base.CompoundEditor
import hextant.sample.ast.IntOperatorApplication

class IntOperatorApplicationEditor(context: Context) : CompoundEditor<IntOperatorApplication>(context) {
    val left by child(IntExprExpander(context))
    val op by child(IntOperatorEditor(context))
    val right by child(IntExprExpander(context))

    override val result: EditorResult<IntOperatorApplication> =
        result3(left, op, right) { l, o, r -> ok(IntOperatorApplication(l, o, r)) }
}