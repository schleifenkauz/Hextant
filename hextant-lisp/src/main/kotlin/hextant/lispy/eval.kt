/**
 * @author Nikolaus Knop
 */

package hextant.lispy

fun evaluate(expr: SExpr): SExpr = when (expr) {
    is Symbol -> Builtin[expr.name] ?: Predefined[expr.name]?.let(::evaluate) ?: fail("Unbound identifier '$expr'")
    is Pair -> {
        val first = evaluate(expr.car)
        val args = expr.cdr.extractList()
        apply(first, args)
    }
    else      -> expr
}

private fun checkArity(function: Builtin, arguments: List<SExpr>) {
    if (function.arity != INFINITE_ARITY) ensure(arguments.size == function.arity) {
        "Arity mismatch: $function expects ${function.arity} arguments but got ${arguments.size}"
    }
}

private fun evaluate(arguments: List<SExpr>): List<SExpr> = arguments.map(::evaluate)

fun apply(func: SExpr, arguments: List<SExpr>): SExpr =
    if (func is BuiltinFunction) {
        val builtin = func.builtin
        checkArity(builtin, arguments)
        if (builtin.isMacro) evaluate(builtin.apply(arguments)) else builtin.apply(evaluate(arguments))
    } else {
        val l = func.extractList()
        ensure(l.size == 3) { "bad syntax: $func" }
        val (lambda, parameters, body) = l
        ensure(lambda.unquote() == "lambda".s) { "$func is not a procedure" }
        val names = parameters.extractList()
            .map { (it.unquote() as? Symbol ?: fail("not a valid parameter name: ${it.unquote()}")).name }
        ensure(names.size == arguments.size) { "Arity mismatch: expected ${names.size} arguments but got ${arguments.size}" }
        val env = names.zip(evaluate(arguments)).toMap()
        evaluate(body.unquote().substitute(env))
    }

private fun SExpr.substitute(env: Map<String, SExpr>): SExpr = when (this) {
    is Symbol -> env[name] ?: this
    is Pair -> Pair(car.substitute(env), cdr.substitute(env))
    else      -> this
}

