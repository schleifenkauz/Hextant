/**
 *@author Nikolaus Knop
 */

package hextant.simple

import hextant.core.editor.ExpanderConfigurator
import hextant.simple.editor.*

object ExprExpanderDelegator : ExpanderConfigurator<ExprEditor<*>>({
    "ref" += ::ReferenceEditor
    "int-literal" += ::IntLiteralEditor
    "false" += { ctx -> BooleanLiteralEditor(ctx, "false") }
    "true" += { ctx -> BooleanLiteralEditor(ctx, "true") }
    for (op in BinaryOperator.values()) {
        op.toString() += { ctx ->
            BinaryExprEditor(ctx).apply {
                operator.setText(op.toString(), undoable = false)
            }
        }
    }
    "call" += ::FunctionCallEditor
    registerTokenInterceptor(Reference, ::ReferenceEditor)
    registerTokenInterceptor(IntLiteral, ::IntLiteralEditor)
})