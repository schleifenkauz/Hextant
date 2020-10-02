/**
 * @author Nikolaus Knop
 */

package hextant.lispy

inline fun <T, reified L : Literal<T>> multiOperator(
    crossinline constructor: (T) -> L,
    crossinline operation: (T, T) -> T
): (List<SExpr>) -> SExpr = { operands ->
    ensure(operands.isNotEmpty()) { "no operands given" }
    operands
        .map { it as? L ?: fail("invalid type of operand $it") }
        .map { it.value }
        .reduce(operation)
        .let(constructor)
}

inline fun <T, reified L : Literal<T>> operator(
    crossinline constructor: (T) -> L,
    crossinline operation: (T, T) -> T
): (List<SExpr>) -> SExpr = { (a, b) ->
    ensure(a is L) { "invalid type of operand $a" }
    ensure(b is L) { "invalid type of operand $b" }
    constructor(operation(a.value, b.value))
}


fun isPair(e: SExpr): Boolean = e is Pair || e is Quoted && e.expr is Pair

val SExpr.car: SExpr
    get() = when (this) {
        is Pair -> car
        is Quoted -> quote(expr.car)
        else      -> fail("$this is not a pair")
    }

val SExpr.cdr: SExpr
    get() = when (this) {
        is Pair -> cdr
        is Quoted -> quote(expr.cdr)
        else      -> fail("$this is not a pair")
    }

fun SExpr.isNil(): Boolean = this is Nil || this is Quoted && expr.isNil()

fun SExpr.isList(): Boolean = isNil() || (isPair(this) && cdr.isList())

fun SExpr.extractList(): List<SExpr> {
    val args = mutableListOf<SExpr>()
    var lst = this
    while (!lst.isNil()) {
        args.add(lst.car)
        lst = lst.cdr
    }
    return args
}

fun truthy(condition: SExpr) = condition != f && condition != nil

fun SExpr.unquote(): SExpr {
    ensure(this is Quoted) { "bad syntax" }
    return expr
}

const val INFINITE_ARITY = -1
