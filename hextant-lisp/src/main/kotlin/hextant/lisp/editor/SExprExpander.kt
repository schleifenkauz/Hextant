/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.Editor
import hextant.completion.NoCompleter
import hextant.core.editor.Expander
import hextant.lisp.editable.*
import hextant.lisp.editor.LispProperties.Internal
import hextant.lisp.editor.LispProperties.fileScope

class SExprExpander(editable: ExpandableSExpr, private val context: Context) :
    Expander<EditableSExpr<*>, ExpandableSExpr>(editable, context, NoCompleter) {
    override fun expand(text: String): EditableSExpr<*>? = text.run {
        asIntLiteral() ?: asDoubleLiteral() ?: asStringLiteral() ?: asCharLiteral() ?: asApply() ?: asGetVal()
    }

    override fun accepts(child: Editor<*>): Boolean = child.editable is EditableSExpr<*>

    private fun String.asGetVal(): EditableGetVal {
        val scope = context[Internal, fileScope]
        return EditableGetVal(scope, this)
    }

    companion object {

        private fun String.asIntLiteral(): EditableSExpr<*>? = toIntOrNull()?.let { EditableIntLiteral(it) }

        private fun String.asDoubleLiteral() = toDoubleOrNull()?.let { EditableDoubleLiteral(it) }

        private fun String.asStringLiteral() = takeIf { startsWith('"') }?.let { EditableStringLiteral(it) }

        private fun String.asCharLiteral(): EditableCharLiteral? {
            return when {
                !startsWith("'")              -> null
                equals("'")                   -> EditableCharLiteral()
                length == 2                   -> EditableCharLiteral(get(1))
                length == 3 && get(2) == '\'' -> EditableCharLiteral(get(1))
                else                          -> null
            }
        }

        private fun String.asApply() = if (this == "(") EditableApply() else null

    }
}