/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.context.*
import hextant.core.editor.ExpanderConfigurator
import hextant.fx.runFXWithTimeout
import hextant.sample.*
import hextant.sample.editor.Scope.Def

object ExprExpanderDelegator : ExpanderConfigurator<ExprEditor<*>>({
    "ref" expand ::ReferenceEditor
    "decimal" expand ::IntLiteralEditor
    "false" expand { ctx -> BooleanLiteralEditor(ctx, "false") }
    "true" expand { ctx -> BooleanLiteralEditor(ctx, "true") }
    for (op in BinaryOperator.values()) {
        op.toString() expand { ctx ->
            BinaryExprEditor(ctx).withoutUndo {
                operator.setText(op.toString())
            }
        }
    }
    "call" expand ::FunctionCallEditor
    registerInterceptor { item: Def, ctx: Context -> ReferenceEditor(ctx, item.name.toString()) }
    registerInterceptor { item: GlobalFunction, ctx: Context ->
        FunctionCallEditor(ctx).withoutUndo {
            name.setText(item.name.toString())
            arguments.resize(item.parameters.size)
            if (item.parameters.isNotEmpty()) runFXWithTimeout {
                context[EditorControlGroup].getViewOf(arguments.editors.now[0]).receiveFocus()
            }
        }
    }
    registerTokenInterceptor(Reference, ::ReferenceEditor)
    registerTokenInterceptor(IntLiteral, ::IntLiteralEditor)
})