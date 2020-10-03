/**
 * @author Nikolaus Knop
 */

package hextant.lispy

const val VARARG = -1

val String.s: SExpr get() = Symbol(this)

fun lit(i: Int): SExpr = IntLiteral(i)

fun lit(b: Boolean) = BooleanLiteral(b)

val f: SExpr get() = lit(false)

val t: SExpr get() = lit(true)

val nil: SExpr get() = Nil

fun list(vararg exprs: SExpr) = exprs.foldRight(nil) { e, acc -> Pair(e, acc) }

fun quote(e: SExpr): SExpr = if (e is Literal<*>) e else Quotation(e)