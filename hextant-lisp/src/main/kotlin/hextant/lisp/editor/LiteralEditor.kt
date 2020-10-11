/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.editor.TokenType
import hextant.core.view.TokenEditorView
import hextant.lisp.*
import hextant.lisp.rt.RuntimeScope
import validated.*

class LiteralEditor(context: Context, override val scope: RuntimeScopeEditor, text: String = "") :
    TokenEditor<SExpr, TokenEditorView>(context, text), SExprEditor {
    constructor(context: Context, scope: RuntimeScopeEditor, literal: Literal<*>)
            : this(context, scope, literal.toString())

    override fun compile(token: String): Validated<SExpr> =
        compile(token, scope.scope).orElse { valid(Hole(token, scope.scope)) }

    companion object : TokenType<Literal<*>> {
        override fun compile(token: String): Validated<Literal<*>> = compile(token, RuntimeScope.empty())

        private fun compile(token: String, scope: RuntimeScope): Validated<Literal<*>> {
            return when (token) {
                "#t" -> valid(BooleanLiteral(true, scope))
                "#f" -> valid(BooleanLiteral(false, scope))
                else -> token.toIntOrNull().validated { invalid("invalid literal '$token'") }.map { IntLiteral(it) }
            }
        }
    }
}