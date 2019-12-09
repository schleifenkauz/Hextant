/**
 *@author Nikolaus Knop
 */

package hextant.blocky.editor

import hextant.blocky.*
import hextant.codegen.ExpanderConfigurator
import hextant.orNull

object ExprExpanderDelegator : ExpanderConfigurator<ExprEditor<Expr>>({
    registerInterceptor { text, context -> IntLiteral.compile(text).orNull()?.let { IntLiteralEditor(context, text) } }
    registerInterceptor { text, context -> Id.compile(text).orNull()?.let { RefEditor(context, Ref(Id(text))) } }
    registerInterceptor { text, context ->
        BinaryOperator.compile(text).orNull()?.let { op ->
            BinaryExpressionEditor(
                BinaryOperatorEditor(context, op),
                ExprExpander(context),
                ExprExpander(context),
                context
            )
        }
    }
    registerInterceptor { text, context ->
        UnaryOperator.compile(text).orNull()?.let { op ->
            UnaryExpressionEditor(UnaryOperatorEditor(context, op), ExprExpander(context), context)
        }
    }
})