/**
 * @author Nikolaus Knop
 */

package hextant.lispy.rt

import hextant.lispy.*
import hextant.lispy.parser.evaluate

private fun Env.addBuiltin(builtin: Builtin) {
    define(builtin.name, builtin)
}

private fun Env.builtin(name: String, arity: Int, def: (arguments: List<SExpr>, callerEnv: Env) -> SExpr) {
    addBuiltin(Builtin(name, arity, false, def))
}

private fun Env.builtinMacro(name: String, arity: Int, def: (arguments: List<SExpr>, callerEnv: Env) -> SExpr) {
    addBuiltin(Builtin(name, arity, true, def))
}

private inline fun <T, reified L : Literal<T>> Env.multiOperator(
    name: String,
    crossinline constructor: (T) -> L,
    crossinline operation: (T, T) -> T
) = builtin(name, VARARG) { operands, _ ->
    ensure(operands.isNotEmpty()) { "no operands given" }
    operands
        .map { it as? L ?: fail("invalid type of operand $it") }
        .map { it.value }
        .reduce(operation)
        .let(constructor)
}

private inline fun <T, reified L : Literal<T>> Env.operator(
    name: String,
    crossinline constructor: (T) -> L,
    crossinline operation: (T, T) -> T
) = builtin(name, arity = 2) { (a, b), _ ->
    ensure(a is L) { "invalid type of operand $a" }
    ensure(b is L) { "invalid type of operand $b" }
    constructor(operation(a.value, b.value))
}

private inline fun Env.shortCircuitingOperator(
    name: String,
    onCondition: SExpr,
    default: SExpr,
    crossinline condition: (SExpr, Env) -> Boolean
) {
    builtin(name, VARARG) { operands, env ->
        for (o in operands) if (condition(o, env)) return@builtin onCondition
        default
    }
}

private fun define(
    signature: SExpr,
    body: SExpr,
    env: Env,
    isMacro: Boolean
): SExpr {
    val symbols = signature.symbolList()
    val name = symbols.first()
    val parameters = symbols.drop(1)
    val proc = Closure(name, parameters, body, isMacro, closureEnv = env)
    env.define(name, proc)
    return t
}

fun Env.registerBuiltins() {
    multiOperator("+", ::IntLiteral, Int::plus)
    multiOperator("-", ::IntLiteral, Int::minus)
    multiOperator("*", ::IntLiteral, Int::times)
    multiOperator("/", ::IntLiteral, Int::div)
    shortCircuitingOperator("and", onCondition = f, default = t) { o, env -> !truthy(o.evaluate(env)) }
    shortCircuitingOperator("or", onCondition = t, default = f) { o, env -> truthy(o.evaluate(env)) }
    operator("xor", ::BooleanLiteral, Boolean::xor)
    builtin("fail", 1) { (msg), _ -> fail(display(msg)) }
    builtinMacro("assert", 1) { (cond), env ->
        val v = cond.evaluate(env)
        ensure(v) { "Assertion failed: ${display(cond)}" }
        v
    }
    builtinMacro("begin", VARARG) { results, _ -> results.last() }
    builtin("nil?", 1) { (l), _ -> lit(l.isNil()) }
    builtin("pair?", 1) { (p), _ -> lit(p.isPair()) }
    builtin("list?", 1) { (l), _ -> lit(l.isList()) }
    builtinMacro("quote", 1) { (e), _ -> Quoted(e) }
    builtin("car", 1) { (p), _ -> p.car }
    builtin("cdr", 1) { (p), _ -> p.cdr }
    builtin("cons", 2) { (a, b), _ -> Pair(a, b) }
    builtinMacro("if", 3) { (cond, then, otherwise), env ->
        if (truthy(cond.evaluate(env))) then
        else otherwise
    }
    builtin("print", 1) { (v), _ ->
        println(display(v))
        t
    }
    builtin("list", VARARG) { elements, _ -> elements.foldRight(nil) { e, acc -> Pair(e, acc) } }
    builtin("eval", 1) { (e), env -> e.evaluate(env) }
    builtinMacro("lambda", 2) { (parameters, body), env ->
        Closure(null, parameters.symbolList(), body, isMacro = false, closureEnv = env)
    }
    builtinMacro("macro", 2) { (parameters, body), env ->
        Closure(null, parameters.symbolList(), body, isMacro = true, closureEnv = env)
    }
    builtinMacro("defun", 2) { (signature, body), env ->
        define(signature, body, env, false)
    }
    builtinMacro("defmacro", 2) { (signature, body), env ->
        define(signature, body, env, true)
    }
}

fun Env.loadPrelude() {
    val prelude = javaClass.classLoader.getResource("hextant/lispy/prelude.lsp")
        ?: fail("failed to load prelude.lsp")
    evaluate(prelude, this)
}