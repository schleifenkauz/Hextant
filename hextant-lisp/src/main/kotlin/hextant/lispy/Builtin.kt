package hextant.lispy

@Suppress("EnumEntryName")
enum class Builtin(
    val id: String,
    val arity: Int,
    val isMacro: Boolean,
    val definition: (Env, List<SExpr>) -> SExpr
) {
    plus("+", INFINITE_ARITY, false, multiOperator(::IntLiteral, Int::plus)),
    minus("-", INFINITE_ARITY, false, multiOperator(::IntLiteral, Int::minus)),
    times("*", INFINITE_ARITY, false, multiOperator(::IntLiteral, Int::times)),
    div("/", INFINITE_ARITY, false, multiOperator(::IntLiteral, Int::div)),
    and("&", INFINITE_ARITY, true, { env, operands ->
        var r = t
        for (o in operands) if (!truthy(evaluate(o, env))) {
            r = f
            break
        }
        r
    }),
    or("|", INFINITE_ARITY, true, { env, operands ->
        var r = f
        for (o in operands) if (truthy(evaluate(o, env))) {
            r = t
            break
        }
        r
    }),
    xor("^", 2, false, operator(::BooleanLiteral, Boolean::xor)),
    assert("assert", 1, true, { env, (cond) ->
        val v = evaluate(cond, env)
        ensure(v) { "Assertion failed: $cond" }
        v
    }),
    begin("begin", INFINITE_ARITY, false, { _, results -> results.last() }),
    is_nil("nil?", 1, false, { _, (l) -> lit(l.isNil()) }),
    is_pair("pair?", 1, false, { _, (p) -> lit(p.isPair()) }),
    quote("quote", 1, false, { _, (e) -> Quoted(e) }),
    car("car", 1, false, { _, (p) -> p.car }),
    cdr("cdr", 1, false, { _, (p) -> p.cdr }),
    is_list("list?", 1, false, { _, (l) -> lit(l.isList()) }),
    cons("cons", 2, false, { _, (x, xs) -> Pair(x, xs) }),
    `if`("if", 4, true, { env, (cond, then, `else`, otherwise) ->
        ensure(`else` == "else".s) { "bad syntax" }
        if (truthy(evaluate(cond, env))) then
        else otherwise
    }),
    print("print", 1, false, { _, (v) ->
        println(v)
        t
    }),
    list("list", INFINITE_ARITY, false, { _, elements -> elements.foldRight(nil) { e, acc -> Pair(e, acc) } }),
    eval("eval", 1, true, { _, (e) -> e }),
    lambda("lambda", 2, true, { env, (parameters, body) ->
        Closure(env, "[anonymous]", parameters.symbolList(), body, isMacro = false)
    }),
    macro("macro", 2, true, { env, (parameters, body) ->
        Closure(env, "[anonymous]", parameters.symbolList(), body, isMacro = true)
    });

    fun apply(env: Env, arguments: List<SExpr>): SExpr = definition(env, arguments)

    operator fun invoke(env: Env, vararg arguments: SExpr) = apply(env, arguments.asList())

    override fun toString(): String = id

    companion object : Map<String, SExpr> by (values().associate { it.id to BuiltinFunction(it) })
}