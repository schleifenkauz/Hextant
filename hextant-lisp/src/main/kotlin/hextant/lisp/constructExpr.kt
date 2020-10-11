/**
 * @author Nikolaus Knop
 */

package hextant.lisp

import hextant.lisp.rt.RuntimeScope

const val VARARG = -1

val String.s: SExpr get() = Symbol(this)

fun lit(i: Int): SExpr = IntLiteral(i)

fun lit(b: Boolean) = BooleanLiteral(b)

val f: SExpr get() = lit(false)

val t: SExpr get() = lit(true)

val nil: SExpr get() = Nil()

fun list(exprs: List<SExpr>, scope: RuntimeScope = RuntimeScope.empty()) =
    exprs.foldRight(Nil(scope) as SExpr) { e, acc -> Pair(e, acc, scope) }

fun list(vararg exprs: SExpr) = list(exprs.asList())

fun list(scope: RuntimeScope, vararg exprs: SExpr) = list(exprs.asList(), scope)

fun quote(e: SExpr): SExpr = if (e is Literal<*>) e else Quotation(e)

fun normalized(e: SExpr): SExpr = NormalizedSExpr(e)