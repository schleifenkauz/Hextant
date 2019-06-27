/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.*
import hextant.completion.CompletionFactory
import hextant.completion.CompletionStrategy
import hextant.core.editor.ConfiguredExpander
import hextant.core.editor.ExpanderConfig
import hextant.expr.edited.Expr
import hextant.expr.edited.Operator.*

class ExprExpander(
    context: Context
) : ConfiguredExpander<Expr, ExprEditor<Expr>>(config, context), ExprEditor<Expr> {
    constructor(edited: Expr, context: Context) : this(context) {
        val editor = context[EditorFactory].getEditor(edited, context) as ExprEditor<*>
        setEditor(editor)
    }

    constructor(editor: ExprEditor<Expr>?, context: Context) : this(context) {
        if (editor != null) {
            setEditor(editor)
        }
    }

    override fun accepts(editor: Editor<*>): Boolean = editor is ExprEditor

    companion object {
        val config = ExpanderConfig<ExprEditor<Expr>>().apply {
            registerConstant("dec") { context -> IntLiteralEditor(context) }
            registerConstant("+") { context -> OperatorApplicationEditor(Plus, context) }
            registerConstant("-") { context -> OperatorApplicationEditor(Minus, context) }
            registerConstant("*") { context -> OperatorApplicationEditor(Times, context) }
            registerConstant("/") { context -> OperatorApplicationEditor(Div, context) }
            registerConstant("sum") { context -> SumEditor(context) }
            registerInterceptor { text, context ->
                val int = text.toIntOrNull()
                if (int != null) IntLiteralEditor(int, context)
                else null
            }
        }

        val completer = config.completer(CompletionStrategy.simple, CompletionFactory.simple())
    }
}