/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.expr.Expr
import reaktive.value.ReactiveValue

class ExprEditorWithCommandLine(context: Context) : CompoundEditor<Expr>(context) {
    val editor by child(ExprExpander(context))

    override val result: ReactiveValue<Expr?>
        get() = editor.result
}