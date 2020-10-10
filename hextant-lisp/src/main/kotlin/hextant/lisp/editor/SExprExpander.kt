/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.Expander
import hextant.core.editor.ExpanderConfig
import hextant.lisp.Hole
import hextant.lisp.SExpr
import reaktive.value.now
import validated.Validated
import validated.valid

class SExprExpander(context: Context, override val scope: RuntimeScopeEditor, editor: SExprEditor? = null) :
    Expander<SExpr, SExprEditor>(context, editor), SExprEditor {
    override fun expand(text: String): SExprEditor? = config.expand(text, this)

    override fun expand(completion: Any): SExprEditor? = config.expand(completion, this)

    override fun defaultResult(): Validated<SExpr> = valid(Hole(text.now ?: ""))

    companion object {
        val config = ExpanderConfig<SExprEditor, SExprExpander>().apply {
            "symbol" expand { ex -> SymbolEditor(ex.context, ex.scope) }
            "#t" expand { ex -> LiteralEditor(ex.context, ex.scope, "#t") }
            "#f" expand { ex -> LiteralEditor(ex.context, ex.scope, "#f") }
            registerKeys("'", "quote") { ex -> QuotationEditor(ex.context, ex.scope) }
            registerKeys("`", "quasiquote") { ex -> QuasiQuotationEditor(ex.context, ex.scope) }
            registerKeys(",", "unquote") { ex -> UnquoteEditor(ex.context, ex.scope) }
            registerKeys("(", "call") { ex -> CallExprEditor(ex.context, ex.scope) }
            "let" expand { ex -> LetEditor(ex.context, ex.scope) }
            "lambda" expand { ex -> LambdaEditor(ex.context, ex.scope) }
            registerTokenInterceptor(SymbolEditor) { ex, symbol -> SymbolEditor(ex.context, ex.scope, symbol) }
            registerTokenInterceptor(LiteralEditor) { ex, literal -> LiteralEditor(ex.context, ex.scope, literal) }
        }
    }
}