/**
 * @author Nikolaus Knop
 */

package hextant.blocky

import hextant.*
import hextant.codegen.*
import hextant.core.TokenType

@Token
data class Id(private val str: String) {
    override fun toString(): String = str

    companion object : TokenType<Id> {
        override fun compile(token: String): CompileResult<Id> = token
            .takeIf { it.all { c -> c.isLetter() } }
            .okOrErr { "Invalid identifier '$token'" }
            .map(::Id)
    }
}

@Alternative
@Expandable(ExprExpanderDelegator::class, subtypeOf = Expr::class)
sealed class Expr

@Token(subtypeOf = Expr::class)
data class IntLiteral(val value: Int) : Expr() {
    companion object : TokenType<IntLiteral> {
        override fun compile(token: String): CompileResult<IntLiteral> =
            token.toIntOrNull()?.let(::IntLiteral).okOrErr { "Invalid integer literal $token" }
    }
}

@Compound(subtypeOf = Expr::class)
data class Ref(val id: Id) : Expr()

@Alternative
@Expandable(StatementExpanderDelegator::class, subtypeOf = Statement::class)
sealed class Statement

@Compound(subtypeOf = Statement::class)
data class Assign(val name: Id, val value: Expr) : Statement()
