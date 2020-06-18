/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.Expander
import hextant.lisp.SExpr

class SExprExpander(context: Context) : Expander<SExpr, SExprEditor<*>>(context), SExprEditor<SExpr> {
    override fun expand(text: String): SExprEditor<*>? = text.run {
        asIntLiteral() ?: asDoubleLiteral() ?: asStringLiteral() ?: asCharLiteral() ?: asApply() ?: asGetVal()
    }

    private fun String.asGetVal(): GetValEditor {
        return GetValEditor(this, context)
    }

    private fun String.asIntLiteral(): SExprEditor<*>? = toIntOrNull()?.let { IntLiteralEditor(it, context) }

    private fun String.asDoubleLiteral() = toDoubleOrNull()?.let { DoubleLiteralEditor(it, context) }

    private fun String.asStringLiteral() = takeIf { startsWith('"') }?.let { StringLiteralEditor(it, context) }

    private fun String.asCharLiteral(): CharLiteralEditor? {
        return when {
            !startsWith("'")              -> null
            equals("'")                   -> CharLiteralEditor(context)
            length == 2                   -> CharLiteralEditor(get(1), context)
            length == 3 && get(2) == '\'' -> CharLiteralEditor(get(1), context)
            else                          -> null
        }
    }

    private fun String.asApply() = if (this == "(") ApplyEditor(context) else null
}