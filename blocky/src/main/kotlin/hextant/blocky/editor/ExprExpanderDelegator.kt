/**
 *@author Nikolaus Knop
 */

package hextant.blocky.editor

import hextant.blocky.*
import hextant.codegen.ExpanderConfigurator
import validated.orNull

object ExprExpanderDelegator : ExpanderConfigurator<ExprEditor<Expr>>({
    registerInterceptor { text, context -> IntLiteral.compile(text).orNull()?.let { IntLiteralEditor(context, text) } }
    registerInterceptor { text, context ->
        Id.compile(text).orNull()?.let { RefEditor(context).apply { id.setText(text) } }
    }
    registerInterceptor { text, context ->
        BinaryOperator.compile(text).orNull()?.let { operator ->
            BinaryExpressionEditor(context).apply { op.setText(operator.toString()) }
        }
    }
    registerInterceptor { text, context ->
        UnaryOperator.compile(text).orNull()?.let { operator ->
            UnaryExpressionEditor(context).apply { op.setText(operator.toString()) }
        }
    }
})