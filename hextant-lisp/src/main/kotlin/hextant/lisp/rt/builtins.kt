/**
 * @author Nikolaus Knop
 */

package hextant.lisp.rt

import hextant.lisp.*
import hextant.lisp.parser.loadFile
import kotlin.system.exitProcess

private fun RuntimeScope.addBuiltin(builtin: Builtin) {
    define(builtin.name, builtin)
}

private fun RuntimeScope.builtin(
    name: String,
    arity: Int,
    def: (arguments: List<SExpr>, callerScope: RuntimeScope) -> SExpr
) {
    addBuiltin(Builtin(name, arity, false, def))
}

private fun RuntimeScope.builtinMacro(
    name: String,
    arity: Int,
    def: (arguments: List<SExpr>, callerScope: RuntimeScope) -> SExpr
) {
    addBuiltin(Builtin(name, arity, true, def))
}

private inline fun <T, reified L : Literal<T>> RuntimeScope.multiOperator(
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

private inline fun <T, reified L : Literal<T>> RuntimeScope.operator(
    name: String,
    crossinline constructor: (T) -> L,
    crossinline operation: (T, T) -> T
) = builtin(name, arity = 2) { (a, b), _ ->
    ensure(a is L) { "invalid type of operand $a" }
    ensure(b is L) { "invalid type of operand $b" }
    constructor(operation(a.value, b.value))
}

private inline fun RuntimeScope.shortCircuitingOperator(
    name: String,
    onCondition: SExpr,
    default: SExpr,
    crossinline condition: (SExpr, RuntimeScope) -> Boolean
) {
    builtin(name, VARARG) { operands, env ->
        for (o in operands) if (condition(o, env)) return@builtin onCondition
        default
    }
}

private fun define(
    signature: SExpr,
    body: SExpr,
    scope: RuntimeScope,
    isMacro: Boolean
): SExpr {
    val symbols = signature.symbolList()
    val name = symbols.first()
    val parameters = symbols.drop(1)
    val proc = Closure(name, parameters, body, isMacro, closureScope = scope)
    scope.define(name, proc)
    return t
}

fun RuntimeScope.registerBuiltins() {
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
    builtin("begin", VARARG) { results, _ -> results.last() }
    builtin("nil?", 1) { (l), _ -> lit(l.isNil()) }
    builtin("pair?", 1) { (p), _ -> lit(p.isPair()) }
    builtin("list?", 1) { (l), _ -> lit(l.isList()) }
    builtinMacro("quote", 1) { (e), _ -> Quotation(e) }
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
        Closure(null, parameters.symbolList(), body, isMacro = false, closureScope = env)
    }
    builtinMacro("macro", 2) { (parameters, body), env ->
        Closure(null, parameters.symbolList(), body, isMacro = true, closureScope = env)
    }
    builtinMacro("defun", 2) { (signature, body), env ->
        define(signature, body, env, false)
    }
    builtinMacro("defmacro", 2) { (signature, body), env ->
        define(signature, body, env, true)
    }
    builtin("eqv?", 2) { (a, b), _ ->
        lit(a == b)
    }
    builtin("same?", 2) { (a, b), _ ->
        lit(a === b)
    }
    builtinMacro("define", 2) { (sym, expr), env ->
        ensure(sym is Symbol) { "${display(sym)} is not a symbol" }
        val v = expr.evaluate(env)
        env.define(sym.name, v)
        quote(v)
    }
    builtin("set!", 2) { (sym, value), env ->
        ensure(sym is Symbol) { "${display(sym)} is not a symbol" }
        env.set(sym.name, value)
        value
    }
    builtinMacro("setq!", 2) { (sym, expr), env ->
        ensure(sym is Symbol) { "${display(sym)} is not a symbol" }
        val v = expr.evaluate(env)
        env.set(sym.name, v)
        quote(v)
    }
    builtin("setcar!", 2) { (p, v), _ ->
        ensure(p is Pair) { "${display(p)} is not a pair" }
        p.car = v
        v
    }
    builtin("setcdr!", 2) { (p, v), _ ->
        ensure(p is Pair) { "${display(p)} is not a pair" }
        p.cdr = v
        v
    }
    builtinMacro("let", 3) { (sym, value, body), env ->
        ensure(sym is Symbol) { "bad syntax" }
        val e = env.child()
        e.define(sym.name, value.evaluate(env))
        quote(body.evaluate(e))
    }
    builtin("quit", 0) { _, _ -> exitProcess(0) }
}

fun RuntimeScope.loadPrelude() {
    val prelude = javaClass.classLoader.getResource("hextant/lisp/prelude.lsp")
        ?: fail("failed to load prelude.lsp")
    loadFile(prelude, this)
}