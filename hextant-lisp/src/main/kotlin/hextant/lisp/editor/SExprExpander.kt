/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.Editor
import hextant.core.editor.Expander
import hextant.lisp.FileScope
import hextant.lisp.editable.*

class SExprExpander(editable: ExpandableSExpr, context: Context) :
    Expander<EditableSExpr<*>, ExpandableSExpr>(editable, context) {
    override fun expand(text: String): EditableSExpr<*>? = text.run {
        asDoubleLiteral() ?: asIntLiteral() ?: asStringLiteral() ?: asCharLiteral() ?: asApply()
    }

    override fun accepts(child: Editor<*>): Boolean = child is EditableSExpr<*>

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

        private fun String.asGetVal(fileScope: FileScope) = EditableGetVal(fileScope, this)
    }
}