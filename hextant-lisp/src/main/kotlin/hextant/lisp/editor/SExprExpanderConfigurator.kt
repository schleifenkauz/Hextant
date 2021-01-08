/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.core.editor.ExpanderConfigurator
import hextant.lisp.IntLiteral

object SExprExpanderConfigurator : ExpanderConfigurator<SExprEditor<*>>({
    "symbol" expand ::SymbolEditor
    "#t" expand { ctx -> BooleanLiteralEditor(ctx, "#t") }
    "#f" expand { ctx -> BooleanLiteralEditor(ctx, "#f") }
    registerKeys("'", "quote", create = ::QuotationEditor)
    registerKeys("`", "quasiquote", create = ::QuasiQuotationEditor)
    registerKeys(",", "unquote", create = ::UnquoteEditor)
    registerKeys("(", "call", create = ::CallExprEditor)
    "let" expand ::LetEditor
    "lambda" expand ::LambdaEditor
    registerTokenInterceptor(IntLiteral) { ctx, res -> IntLiteralEditor(ctx, res) }
})