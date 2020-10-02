/**
 * @author Nikolaus Knop
 */

package hextant.lispy

inline fun <T, reified L : Literal<T>> multiOperator(
    crossinline constructor: (T) -> L,
    crossinline operation: (T, T) -> T
): (Env, List<SExpr>) -> SExpr = { _, operands ->
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
): (Env, List<SExpr>) -> SExpr = { _, (a, b) ->
    ensure(a is L) { "invalid type of operand $a" }
    ensure(b is L) { "invalid type of operand $b" }
    constructor(operation(a.value, b.value))
}


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

const val INFINITE_ARITY = -1
