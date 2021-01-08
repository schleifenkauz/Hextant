/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.expr.Expr
import validated.Validated
import validated.reaktive.ReactiveValidated

class ExprEditorWithCommandLine(context: Context) : CompoundEditor<Validated<Expr>>(context) {
    val editor by child(ExprExpander(context))

    override val result: ReactiveValidated<Expr>
        get() = editor.result
}