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

class SymbolEditor(context: Context, override val scope: RuntimeScopeEditor, text: String = "") :
    TokenEditor<SExpr, TokenEditorView>(context, text), SExprEditor {
    constructor(context: Context, scope: RuntimeScopeEditor, symbol: Symbol) : this(context, scope, symbol.name)

    override fun compile(token: String): Validated<SExpr> =
        compile(token, scope.scope).orElse { valid(Hole(token, scope.scope)) }

    companion object : TokenType<Symbol> {
        override fun compile(token: String): Validated<Symbol> = compile(token, RuntimeScope.empty())

        private fun compile(token: String, scope: RuntimeScope) =
            if (isValid(token)) valid(Symbol(token, scope)) else invalid("invalid identifier '$token'")

        private val forbidden = "()[]'`,".toSet()

        private fun isValid(token: String) = token.isNotEmpty() && !token[0].isDigit() && token.none { it in forbidden }
    }
}