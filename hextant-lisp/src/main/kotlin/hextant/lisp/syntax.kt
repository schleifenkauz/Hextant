/**
 * @author Nikolaus Knop
 */

package hextant.lisp

import hextant.core.editor.TokenType
import hextant.lisp.rt.RuntimeScope
import hextant.lisp.rt.evaluate
import validated.*

sealed class SExpr {
    abstract val scope: RuntimeScope
}

data class Hole(val text: String, override val scope: RuntimeScope = RuntimeScope.empty()) : SExpr() {
    override fun toString(): String = "?$text"
}

data class Symbol(val name: String, override val scope: RuntimeScope = RuntimeScope.empty()) : SExpr() {
    override fun toString(): String = name

    companion object : TokenType<Symbol> {
        fun isValid(symbol: Symbol) = symbol.name.none { it.isWhitespace() }

        override fun compile(token: String): Validated<Symbol> = valid(Symbol(token))
    }
}

sealed class Literal<T : Any> : SExpr() {
    abstract val value: T
}

data class IntLiteral(
    override val value: Int,
    override val scope: RuntimeScope = RuntimeScope.empty()
) : Literal<Int>() {
    override fun toString(): String = "$value"

    companion object : TokenType<IntLiteral> {
        override fun compile(token: String): Validated<IntLiteral> =
            token.toIntOrNull().validated { invalid("invalid integer literal '$token") }.map(::IntLiteral)
    }
}

data class BooleanLiteral(
    override val value: Boolean,
    override val scope: RuntimeScope = RuntimeScope.empty()
) : Literal<Boolean>() {
    override fun toString(): String = if (value) "#t" else "#f"

    companion object : TokenType<BooleanLiteral> {
        override fun compile(token: String): Validated<BooleanLiteral> = when (token) {
            "#t" -> valid(BooleanLiteral(true))
            "#f" -> valid(BooleanLiteral(false))
            else -> invalid("invalid boolean literal '$token'")
        }
    }
}

data class Pair(var car: SExpr, var cdr: SExpr, override val scope: RuntimeScope = RuntimeScope.empty()) : SExpr()

data class Nil(override val scope: RuntimeScope = RuntimeScope.empty()) : SExpr() {
    override fun toString(): String = "Nil"
}

data class Quotation(val quoted: SExpr, override val scope: RuntimeScope = RuntimeScope.empty()) : SExpr()

data class QuasiQuotation(val quoted: SExpr, override val scope: RuntimeScope = RuntimeScope.empty()) : SExpr()

data class Unquote(val expr: SExpr, override val scope: RuntimeScope = RuntimeScope.empty()) : SExpr()

data class NormalizedSExpr(val expr: SExpr) : SExpr() {
    override val scope = RuntimeScope.empty()
}

abstract class Procedure : SExpr() {
    abstract val name: String?

    abstract val isMacro: Boolean

    abstract val arity: Int

    abstract fun call(arguments: List<SExpr>, callerScope: RuntimeScope): SExpr
}

data class Builtin(
    override val name: String,
    override val arity: Int,
    override val isMacro: Boolean,
    private val def: (arguments: List<SExpr>, callerScope: RuntimeScope) -> SExpr
) : Procedure() {
    override val scope: RuntimeScope get() = RuntimeScope.empty()

    override fun call(arguments: List<SExpr>, callerScope: RuntimeScope): SExpr = def(arguments, callerScope)
}

data class Closure(
    override val name: String?,
    val parameters: List<String>,
    val body: SExpr,
    override val isMacro: Boolean,
    override val scope: RuntimeScope
) : Procedure() {
    override val arity: Int
        get() = parameters.size

    override fun call(arguments: List<SExpr>, callerScope: RuntimeScope): SExpr {
        val callEnv = scope.child()
        for ((name, value) in parameters.zip(arguments)) callEnv.define(name, value)
        return body.evaluate(callEnv)
    }
}