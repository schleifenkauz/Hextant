/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.lisp.*
import validated.reaktive.ReactiveValidated
import validated.reaktive.composeReactive

class LambdaEditor(context: Context) : CompoundEditor<SExpr>(context), SExprEditor<SExpr>,
                                       RuntimeScopeAware by RuntimeScopeAware.delegate() {
    val parameters = SymbolListEditor(context)
    val body = SExprExpander(context)

    override val result: ReactiveValidated<SExpr> = composeReactive(parameters.result, body.result) { params, b ->
        list("lambda".s, list(params), b)
    }
}