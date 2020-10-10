/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.lisp.*
import validated.reaktive.ReactiveValidated
import validated.reaktive.mapValidated

class UnquoteEditor(context: Context, override val scope: RuntimeScopeEditor) : CompoundEditor<SExpr>(context),
                                                                                SExprEditor {
    val expr by child(SExprExpander(context, scope))

    override val result: ReactiveValidated<SExpr> = expr.result.mapValidated { q -> list("unquote".s, q) }
}