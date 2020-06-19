/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.core.editor.composeResult
import hextant.sample.ast.IntOperatorApplication
import validated.reaktive.ReactiveValidated

class IntOperatorApplicationEditor(context: Context) : CompoundEditor<IntOperatorApplication>(context) {
    val left by child(IntExprExpander(context))
    val op by child(IntOperatorEditor(context))
    val right by child(IntExprExpander(context))

    override val result: ReactiveValidated<IntOperatorApplication> = composeResult(left, op, right)
}