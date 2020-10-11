/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.lisp.*
import validated.reaktive.ReactiveValidated
import validated.reaktive.composeReactive

class LambdaEditor(context: Context, override val scope: RuntimeScopeEditor) : CompoundEditor<SExpr>(context),
                                                                               SExprEditor {
    val parameters by child(SymbolListEditor(context, scope))
    val body by child(SExprExpander(context, scope))

    override val result: ReactiveValidated<SExpr> = composeReactive(parameters.result, body.result) { params, b ->
        quote(list(scope.scope, Symbol("lambda", scope.scope), list(params, scope.scope), b))
    }
}