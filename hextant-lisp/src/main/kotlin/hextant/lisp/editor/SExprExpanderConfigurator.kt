/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.core.editor.ExpanderConfigurator
import hextant.lisp.IntLiteral
import hextant.lisp.Symbol

object SExprExpanderConfigurator : ExpanderConfigurator<SExprEditor<*>>({
    "symbol" expand ::SymbolEditor
    "#t" expand { ctx -> ScalarEditor(ctx, "#t") }
    "#f" expand { ctx -> ScalarEditor(ctx, "f") }
    registerKeys("'", "quote", create = ::QuotationEditor)
    registerKeys("`", "quasiquote", create = ::QuasiQuotationEditor)
    registerKeys(",", "unquote", create = ::UnquoteEditor)
    registerKeys("(", "call", create = ::CallExprEditor)
    "let" expand ::LetEditor
    "lambda" expand ::LambdaEditor
    registerTokenInterceptor(Symbol, ::SymbolEditor)
    registerTokenInterceptor(IntLiteral, ::ScalarEditor)
})