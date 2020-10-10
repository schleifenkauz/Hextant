/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.editor.TokenType
import hextant.core.view.TokenEditorView
import hextant.lisp.*
import validated.*

class LiteralEditor(context: Context, override val scope: RuntimeScopeEditor, text: String = "") :
    TokenEditor<SExpr, TokenEditorView>(context, text), SExprEditor {
    constructor(context: Context, scope: RuntimeScopeEditor, literal: Literal<*>)
            : this(context, scope, literal.toString())

    override fun compile(token: String): Validated<SExpr> = LiteralEditor.compile(token).orElse { valid(Hole(token)) }

    companion object : TokenType<Literal<*>> {
        override fun compile(token: String): Validated<Literal<*>> = when (token) {
            "#t" -> valid(BooleanLiteral(true))
            "#f" -> valid(BooleanLiteral(false))
            else -> token.toIntOrNull().validated { invalid("invalid literal '$token'") }.map { IntLiteral(it) }
        }
    }
}