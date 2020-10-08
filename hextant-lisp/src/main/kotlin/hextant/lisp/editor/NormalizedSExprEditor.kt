/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.lisp.SExpr
import validated.reaktive.ReactiveValidated

class NormalizedSExprEditor(context: Context, val wrapped: SExprEditor<*>) : CompoundEditor<SExpr>(context),
                                                                             SExprEditor<SExpr>,
                                                                             RuntimeScopeAware by RuntimeScopeAware.delegate() {
    override val result: ReactiveValidated<SExpr> get() = wrapped.result
}