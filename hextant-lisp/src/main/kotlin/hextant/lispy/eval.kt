/**
 * @author Nikolaus Knop
 */

package hextant.lispy

fun evaluate(expr: SExpr, env: Env): SExpr = when (expr) {
    is Symbol -> env.get(expr.name)
    is Quoted -> expr.expr
    is Pair -> {
        val first = evaluate(expr.car, env)
        val args = expr.cdr.extractList()
        apply(first, args, env)
    }
    else      -> expr
}

private fun checkArity(arity: Int, function: SExpr, arguments: List<SExpr>) {
    if (arity != INFINITE_ARITY) ensure(arguments.size == arity) {
        "Arity mismatch: $function expects $arity arguments but got ${arguments.size}"
    }
}

private fun evaluate(arguments: List<SExpr>, env: Env): List<SExpr> = arguments.map { evaluate(it, env) }

fun apply(func: SExpr, arguments: List<SExpr>, env: Env): SExpr = when (func) {
    is BuiltinFunction -> {
        val builtin = func.builtin
        checkArity(builtin.arity, func, arguments)
        if (builtin.isMacro) evaluate(builtin.apply(env, arguments), env)
        else builtin.apply(env, evaluate(arguments, env))
    }
    is Closure -> {
        checkArity(func.parameters.size, func, arguments)
        val e = env.child()
        val args = if (func.isMacro) arguments else evaluate(arguments, env)
        for ((name, value) in func.parameters.zip(args)) {
            e.define(name, value)
        }
        val result = evaluate(func.body, e)
        if (func.isMacro) evaluate(result, env) else result
    }
    else               -> fail("not a procedure: $func")
}