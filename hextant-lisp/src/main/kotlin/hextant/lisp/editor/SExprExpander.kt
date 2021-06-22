package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.ConfiguredExpander
import hextant.core.editor.Expander
import hextant.core.editor.ExpanderConfig
import hextant.lisp.*
import reaktive.value.now

class SExprExpander(context: Context, editor: SExprEditor<*>? = null) :
    ConfiguredExpander<SExpr, SExprEditor<*>>(config, Scalar, context, editor), SExprEditor<SExpr> {
    companion object {
        val config = ExpanderConfig<SExprEditor<*>>().apply {
            registerKeys("'", "quote", create = ::QuotationEditor)
            registerKeys("`", "quasiquote", create = ::QuasiQuotationEditor)
            registerKeys(",", "unquote", create = ::UnquoteEditor)
            registerKeys("(", "call", create = ::CallExprEditor)
        }
    }
}