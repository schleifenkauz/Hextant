/**
 * @author Nikolaus Knop
 */

package hextant.lisp

import hextant.codegen.*
import hextant.core.editor.TokenType
import hextant.lisp.editor.SExprEditor
import hextant.lisp.editor.SExprExpanderConfigurator
import hextant.lisp.rt.RuntimeScope
import hextant.lisp.rt.evaluate

@EditorInterface(SExprEditor::class)
@Expandable(SExprExpanderConfigurator::class, nodeType = SExpr::class)
@EditableList
sealed class SExpr

@Token(nodeType = SExpr::class)
@EditableList
data class Symbol(val name: String) : SExpr() {
    override fun toString(): String = name

    companion object : TokenType<Symbol?> {
        fun isValid(symbol: Symbol) = symbol.name.none { it.isWhitespace() }

        override fun compile(token: String): Symbol? = Symbol(token).takeIf { isValid(it) }
    }
}

@Token(nodeType = SExpr::class)
data class IntLiteral(override val value: Int) : Literal<Int>() {
    override fun toString(): String = "$value"

    companion object : TokenType<IntLiteral?> {
        override fun compile(token: String): IntLiteral? = token.toIntOrNull()?.let(::IntLiteral)
    }
}

sealed class Literal<T : Any> : SExpr() {
    abstract val value: T
}

@Token(nodeType = SExpr::class)
data class BooleanLiteral(override val value: Boolean) : Literal<Boolean>() {
    companion object : TokenType<BooleanLiteral?> {
        override fun compile(token: String): BooleanLiteral? = when (token) {
            "#t" -> BooleanLiteral(true)
            "#f" -> BooleanLiteral(false)
            else -> null
        }
    }
}

data class Pair(var car: SExpr, var cdr: SExpr) : SExpr()

object Nil : SExpr() {
    override fun toString(): String = "Nil"
}

@Compound(nodeType = SExpr::class)
data class Quotation(val quoted: SExpr) : SExpr()

@Compound(nodeType = SExpr::class)
data class QuasiQuotation(val quoted: SExpr) : SExpr()

@Compound(nodeType = SExpr::class)
data class Unquote(val expr: SExpr) : SExpr()

@Compound(nodeType = SExpr::class)
data class NormalizedSExpr(val expr: SExpr) : SExpr()

@Compound(nodeType = SExpr::class)
fun lambda(parameters: List<Symbol>, body: SExpr) = quote(list("lambda".s, list(parameters), body))

@Compound(nodeType = SExpr::class)
fun let(name: Symbol, value: SExpr, body: SExpr) = list("let".s, name, value, body)

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
    override fun call(arguments: List<SExpr>, callerScope: RuntimeScope): SExpr = def(arguments, callerScope)
}

data class Closure(
    override val name: String?,
    val parameters: List<String>,
    val body: SExpr,
    override val isMacro: Boolean,
    val closureScope: RuntimeScope
) : Procedure() {
    override val arity: Int
        get() = parameters.size

    override fun call(arguments: List<SExpr>, callerScope: RuntimeScope): SExpr {
        val callEnv = closureScope.child()
        for ((name, value) in parameters.zip(arguments)) callEnv.define(name, value)
        return body.evaluate(callEnv)
    }
}