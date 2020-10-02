/**
 * @author Nikolaus Knop
 */

package hextant.lispy

sealed class SExpr

data class Symbol(val name: String) : SExpr() {
    override fun toString(): String = name
}

val String.s: SExpr get() = Symbol(this)

data class IntLiteral(override val value: Int) : Literal<Int>() {
    override fun toString(): String = "$value"
}

fun lit(i: Int): SExpr = IntLiteral(i)

sealed class Literal<T : Any> : SExpr() {
    abstract val value: T
}

data class BooleanLiteral(override val value: Boolean) : Literal<Boolean>() {
    override fun toString(): String = "#" + value.toString().first()
}

fun lit(b: Boolean) = BooleanLiteral(b)

val f: SExpr get() = lit(false)

val t: SExpr get() = lit(true)

data class Pair(val car: SExpr, val cdr: SExpr) : SExpr() {
    override fun toString(): String = if (isList()) extractList().joinToString(" ", "(", ")") else "($car . $cdr)"
}

object Nil : SExpr() {
    override fun toString(): String = "'()"
}

val nil: SExpr get() = Nil

infix fun SExpr.cons(cdr: SExpr): SExpr = Pair(this, cdr)

fun call(vararg exprs: SExpr) = exprs.foldRight(nil) { e, acc -> Pair(e, acc) }

data class Quoted(val expr: SExpr) : SExpr() {
    override fun toString(): String = "'$expr"
}

fun quote(e: SExpr): SExpr = if (e is Literal<*>) e else Quoted(e)

data class BuiltinFunction(val builtin: Builtin) : SExpr() {
    override fun toString(): String = "<procedure ${builtin.id} #${builtin.arity}>"
}

data class Closure(
    val env: Env,
    val name: String,
    val parameters: List<String>,
    val body: SExpr,
    val isMacro: Boolean
) : SExpr() {
    override fun toString(): String = "<procedure $name #${parameters.size}>"
}