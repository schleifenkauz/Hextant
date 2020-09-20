/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.context.Context
import hextant.context.EditorControlGroup
import hextant.core.editor.ExpanderConfigurator
import hextant.fx.runFXWithTimeout
import hextant.sample.*
import hextant.sample.editor.Scope.Binding

object ExprExpanderDelegator : ExpanderConfigurator<ExprEditor<*>>({
    "ref" expand ::ReferenceEditor
    "decimal" expand ::IntLiteralEditor
    "false" expand { ctx -> BooleanLiteralEditor(ctx, "false") }
    "true" expand { ctx -> BooleanLiteralEditor(ctx, "true") }
    for (op in BinaryOperator.values()) {
        op.toString() expand { ctx ->
            BinaryExprEditor(ctx).apply {
                operator.setText(op.toString(), undoable = false)
            }
        }
    }
    "call" expand ::FunctionCallEditor
    registerInterceptor { item: Binding, ctx: Context -> ReferenceEditor(ctx, item.name.toString()) }
    registerInterceptor { item: GlobalFunction, ctx: Context ->
        FunctionCallEditor(ctx).apply {
            name.setText(item.name.toString(), undoable = false)
            arguments.resize(item.parameters.size)
            if (item.parameters.isNotEmpty()) runFXWithTimeout {
                context[EditorControlGroup].getViewOf(arguments.editors.now[0]).receiveFocus()
            }
        }
    }
    registerTokenInterceptor(Reference, ::ReferenceEditor)
    registerTokenInterceptor(IntLiteral, ::IntLiteralEditor)
})