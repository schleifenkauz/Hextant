/**
 *@author Nikolaus Knop
 */

package hextant.blocky.editor

import hextant.blocky.*
import hextant.core.editor.ExpanderConfigurator

object ExprExpanderDelegator : ExpanderConfigurator<ExprEditor<Expr>>({
    registerInterceptor { text, context -> IntLiteral.compile(text)?.let { IntLiteralEditor(context, text) } }
    registerInterceptor { text, context ->
        Id.compile(text)?.let { RefEditor(context).apply { id.setText(text) } }
    }
    registerInterceptor { text, context ->
        BinaryOperator.compile(text)?.let { operator ->
            BinaryExpressionEditor(context).apply { op.setText(operator.toString()) }
        }
    }
    registerInterceptor { text, context ->
        UnaryOperator.compile(text)?.let { operator ->
            UnaryExpressionEditor(context).apply { op.setText(operator.toString()) }
        }
    }
})