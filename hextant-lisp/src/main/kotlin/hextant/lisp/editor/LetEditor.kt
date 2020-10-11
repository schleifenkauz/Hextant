/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.lisp.*
import validated.reaktive.ReactiveValidated
import validated.reaktive.composeReactive

class LetEditor(context: Context, override val scope: RuntimeScopeEditor) : CompoundEditor<SExpr>(context),
                                                                            SExprEditor {
    val name by child(SymbolEditor(context, scope))
    val value by child(SExprExpander(context, scope))
    val body by child(SExprExpander(context, scope))

    override val result: ReactiveValidated<SExpr> = composeReactive(name.result, value.result, body.result) { n, v, b ->
        list(scope.scope, Symbol("let", scope.scope), n, v, b)
    }
}