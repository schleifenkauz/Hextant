/**
 * @author Nikolaus Knop
 */

package hextant.lisp

import hextant.codegen.*
import hextant.core.editor.TokenType
import hextant.lisp.editor.*
import hextant.lisp.rt.RuntimeScope
import validated.*

@EditorInterface(SExprEditor::class, RuntimeScopeAware::class)
@Expandable(SExprExpanderConfigurator::class, subtypeOf = SExpr::class)
@EditableList
sealed class SExpr

@Token(subtypeOf = SExpr::class)
@EditableList
data class Symbol(val name: String) : SExpr() {
    override fun toString(): String = name

    companion object : TokenType<Symbol> {
        fun isValid(symbol: Symbol) = symbol.name.none { it.isWhitespace() }

        override fun compile(token: String): Validated<Symbol> = valid(Symbol(token))
    }
}

@Token(subtypeOf = SExpr::class)
data class IntLiteral(override val value: Int) : Literal<Int>() {
    override fun toString(): String = "$value"

    companion object : TokenType<IntLiteral> {
        override fun compile(token: String): Validated<IntLiteral> =
            token.toIntOrNull().validated { invalid("invalid integer literal '$token") }.map(::IntLiteral)
    }
}

sealed class Literal<T : Any> : SExpr() {
    abstract val value: T
}

@Token(subtypeOf = SExpr::class)
data class BooleanLiteral(override val value: Boolean) : Literal<Boolean>() {
    companion object : TokenType<BooleanLiteral> {
        override fun compile(token: String): Validated<BooleanLiteral> = when (token) {
            "#t" -> valid(BooleanLiteral(true))
            "#f" -> valid(BooleanLiteral(false))
            else -> invalid("invalid boolean literal '$token'")
        }
    }
}

data class Pair(var car: SExpr, var cdr: SExpr) : SExpr()

object Nil : SExpr() {
    override fun toString(): String = "Nil"
}

@Compound(subtypeOf = SExpr::class)
data class Quotation(val quoted: SExpr) : SExpr()

@Compound(subtypeOf = SExpr::class)
data class QuasiQuotation(val quoted: SExpr) : SExpr()

@Compound(subtypeOf = SExpr::class)
data class Unquote(val expr: SExpr) : SExpr()

abstract class Procedure : SExpr() {
    abstract val name: String?

    abstract val isMacro: Boolean

    abstract val arity: Int

    abstract fun call(arguments: List<SExpr>, callerScope: RuntimeScope): SExpr
}