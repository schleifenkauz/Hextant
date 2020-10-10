/**
 * @author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.lisp.NormalizedSExpr
import hextant.lisp.SExpr
import validated.reaktive.ReactiveValidated
import validated.reaktive.mapValidated

class NormalizedSExprEditor(context: Context, override val scope: RuntimeScopeEditor) : CompoundEditor<SExpr>(context),
                                                                                        SExprEditor {
    val expr by child(SExprExpander(context, scope))

    override val result: ReactiveValidated<SExpr> = expr.result.mapValidated { NormalizedSExpr(it) }
}