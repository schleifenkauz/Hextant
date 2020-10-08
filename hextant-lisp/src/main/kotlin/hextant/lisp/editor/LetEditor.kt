/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.lisp.*
import validated.reaktive.ReactiveValidated
import validated.reaktive.composeReactive

class LetEditor(context: Context) : CompoundEditor<SExpr>(context), SExprEditor<SExpr>,
                                    RuntimeScopeAware by RuntimeScopeAware.delegate() {
    val name = SymbolEditor(context)
    val value = SExprExpander(context)
    val body = SExprExpander(context)

    override val result: ReactiveValidated<SExpr> = composeReactive(name.result, value.result, body.result) { n, v, b ->
        list("let".s, n, v, b)
    }
}