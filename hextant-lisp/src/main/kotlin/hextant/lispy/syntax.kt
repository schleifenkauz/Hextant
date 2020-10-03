/**
 * @author Nikolaus Knop
 */

package hextant.lispy

import hextant.lispy.rt.Env

sealed class SExpr

data class Symbol(val name: String) : SExpr()

data class IntLiteral(override val value: Int) : Literal<Int>()

sealed class Literal<T : Any> : SExpr() {
    abstract val value: T
}

data class BooleanLiteral(override val value: Boolean) : Literal<Boolean>()

data class Pair(var car: SExpr, var cdr: SExpr) : SExpr()

object Nil : SExpr() {
    override fun toString(): String = "Nil"
}

data class Quotation(val quoted: SExpr) : SExpr()

data class QuasiQuotation(val quoted: SExpr) : SExpr()

data class Unquote(val expr: SExpr) : SExpr()

abstract class Procedure : SExpr() {
    abstract val name: String?

    abstract val isMacro: Boolean

    abstract val arity: Int

    abstract fun call(arguments: List<SExpr>, callerEnv: Env): SExpr
}