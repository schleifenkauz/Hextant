/**
 * @author Nikolaus Knop
 */

package hextant.lisp.debug

import hextant.context.Context
import hextant.lisp.*
import hextant.lisp.editor.*
import hextant.lisp.rt.*
import hextant.plugin.Aspects

fun <E : SExprEditor<*>> Aspects.doTransform(
    expr: E,
    visitor: SExprVisitor
): SExprEditor<*> {
    val e = get<Evaluation<E>>(expr)
    return with(e) { transform(expr, visitor) }
}

fun reconstructEditor(context: Context, expr: SExpr): SExprExpander = SExprExpander(context).apply { reconstruct(expr) }

fun SExprExpander.reconstruct(expr: SExpr) {
    when (expr) {
        is Symbol -> setEditor(SymbolEditor(context, expr), undoable = false)
        is IntLiteral -> setEditor(IntLiteralEditor(context, expr), undoable = false)
        is BooleanLiteral -> setEditor(BooleanLiteralEditor(context, expr), undoable = false)
        is Pair -> {
            assert(expr.isList()) { "Can't reconstruct expression ${(display(expr))}" }
            val e = CallExprEditor(context)
            setEditor(e, undoable = false)
            for (ex in expr.extractList()) {
                e.expressions.addLast(reconstructEditor(context, ex), undoable = false)
            }
        }
        Nil -> setEditor(CallExprEditor(context), undoable = false)
        is Quotation -> setEditor(QuotationEditor(context), false)
        is QuasiQuotation -> setEditor(QuasiQuotationEditor(context), false)
        is Unquote -> setEditor(UnquoteEditor(context), false)
        is Closure -> {
            val e = LambdaEditor(context)
            setEditor(e, undoable = false)
            for (param in expr.parameters) {
                e.parameters.addLast(SymbolEditor(context, param), undoable = false)
            }
            e.body.reconstruct(expr.body)
            e.context[RuntimeScope] = expr.closureScope
        }
        else              -> fail("Can't reconstruct expression ${display(expr)}")
    }
}