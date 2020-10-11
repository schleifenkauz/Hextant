/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.lisp.*
import validated.reaktive.ReactiveValidated
import validated.reaktive.mapValidated

class QuasiQuotationEditor(context: Context, override val scope: RuntimeScopeEditor) : CompoundEditor<SExpr>(context),
                                                                                       SExprEditor {
    val quoted by child(SExprExpander(context, scope))

    override val result: ReactiveValidated<SExpr> =
        quoted.result.mapValidated { q -> list(scope.scope, Symbol("quasiquote", scope.scope), q) }
}