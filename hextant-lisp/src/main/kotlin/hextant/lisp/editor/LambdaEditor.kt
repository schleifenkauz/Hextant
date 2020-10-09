/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.lisp.*
import validated.reaktive.ReactiveValidated
import validated.reaktive.composeReactive

class LambdaEditor(context: Context) : CompoundEditor<SExpr>(context), SExprEditor<SExpr> {
    val parameters by child(SymbolListEditor(context))
    val body by child(SExprExpander(context))

    override val result: ReactiveValidated<SExpr> = composeReactive(parameters.result, body.result) { params, b ->
        quote(list("lambda".s, list(params), b))
    }
}