/**
 * @author Nikolaus Knop
 */

package hextant.lispy.rt

import hextant.lispy.*

fun SExpr.isPair(): Boolean = this is Pair || this is Quoted && expr.isPair()

val SExpr.car: SExpr
    get() = when (this) {
        is Pair -> car
        is Quoted -> expr.car
        else      -> fail("$this is not a pair")
    }

val SExpr.cdr: SExpr
    get() = when (this) {
        is Pair -> cdr
        is Quoted -> expr.cdr
        else      -> fail("$this is not a pair")
    }

fun SExpr.isNil(): Boolean = this is Nil || this is Quoted && expr.isNil()

fun SExpr.isList(): Boolean = isNil() || (isPair() && cdr.isList())

fun SExpr.extractList(): List<SExpr> {
    val args = mutableListOf<SExpr>()
    var lst = this
    while (!lst.isNil()) {
        args.add(lst.car)
        lst = lst.cdr
    }
    return args
}

fun SExpr.symbolList() = extractList().map { (it as Symbol).name }

fun truthy(condition: SExpr) = condition != f && condition != nil


