/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.codegen.ProvideFeature
import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.lisp.SExpr
import hextant.lisp.list
import reaktive.value.ReactiveValue

@ProvideFeature
class CallExprEditor(context: Context) : CompoundEditor<SExpr>(context), SExprEditor<SExpr> {
    val expressions by child(SExprListEditor(context))

    override val result: ReactiveValue<SExpr?> = expressions.result.mapValidated { exprs -> list(exprs) }
}