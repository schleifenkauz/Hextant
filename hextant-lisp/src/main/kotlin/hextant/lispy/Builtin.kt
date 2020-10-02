package hextant.lispy

@Suppress("EnumEntryName")
enum class Builtin(val id: String, val arity: Int, val isMacro: Boolean, val definition: (List<SExpr>) -> SExpr) {
    plus("+", INFINITE_ARITY, false, multiOperator(::IntLiteral, Int::plus)),
    minus("-", INFINITE_ARITY, false, multiOperator(::IntLiteral, Int::minus)),
    times("*", INFINITE_ARITY, false, multiOperator(::IntLiteral, Int::times)),
    div("/", INFINITE_ARITY, false, multiOperator(::IntLiteral, Int::div)),
    and("&", INFINITE_ARITY, true, { operands ->
        var r = t
        for (o in operands) if (!truthy(evaluate(o))) {
            r = f
            break
        }
        r
    }),
    or("|", INFINITE_ARITY, true, { operands ->
        var r = f
        for (o in operands) if (truthy(evaluate(o))) {
            r = t
            break
        }
        r
    }),
    xor("^", 2, false, operator(::BooleanLiteral, Boolean::xor)),
    assert("assert", 1, true, { (cond) ->
        val v = evaluate(cond)
        ensure(v) { "Assertion failed: $cond" }
        v
    }),
    begin("begin", INFINITE_ARITY, false, { results -> results.last() }),
    is_nil("nil?", 1, false, { (l) -> lit(l.isNil()) }),
    is_pair("pair?", 1, false, { (p) -> lit(isPair(p)) }),
    quote("quote", 1, true, { (e) -> Quoted(e) }),
    car("car", 1, false, { (p) -> p.car }),
    cdr("cdr", 1, false, { (p) -> p.cdr }),
    is_list("list?", 1, false, { (l) -> lit(l.isList()) }),
    cons("cons", 2, false, { (x, xs) -> Pair(x, xs) }),
    `if`("if", 4, true, { (cond, then, `else`, otherwise) ->
        ensure(`else` == "else".s) { "bad syntax" }
        if (truthy(evaluate(cond))) call("eval".s, then)
        else call("eval".s, otherwise)
    }),
    eval("eval", 1, false, { (e) ->
        if (e is Quoted) e.expr
        else e
    }),
    lambda("lambda", 2, true, { (parameters, body) ->
        ensure(parameters.isList()) { "bad syntax" }
        quote(call("lambda".s, parameters, body))
    });

    fun apply(arguments: List<SExpr>) = definition(arguments)

    operator fun invoke(vararg arguments: SExpr) = apply(arguments.asList())

    override fun toString(): String = id

    companion object : Map<String, SExpr> by (values().associate { it.id to BuiltinFunction(it) })
}