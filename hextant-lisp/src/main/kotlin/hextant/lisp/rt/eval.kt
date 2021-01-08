/**
 * @author Nikolaus Knop
 */

package hextant.lisp.rt

import hextant.lisp.*

fun SExpr.evaluate(scope: RuntimeScope = RuntimeScope.root()): SExpr =
    when (this) {
        is NormalizedSExpr -> this
        is Symbol -> scope.get(name) ?: fail("unbound variable $name")
        is QuasiQuotation -> normalized(quoted.unquoteAfterCommas(scope))
        is Unquote -> fail("comma is illegal outside of quasi-quotation")
        is Pair -> {
            ensure(isList())
            val proc = car.evaluate(scope).unwrap()
            ensure(proc is Procedure) { "${display(proc)} is not a procedure" }
            val args = cdr.extractList()
            apply(proc, args, scope)
        }
        is Nil -> fail("Illegal empty application ()")
        is Literal<*>, is Procedure, is Quotation -> this
    }

private fun SExpr.unquoteAfterCommas(scope: RuntimeScope): SExpr = when (this) {
    is Pair -> Pair(car.unquoteAfterCommas(scope), cdr.unquoteAfterCommas(scope))
    is Unquote -> expr.evaluate(scope)
    is QuasiQuotation -> fail("Nested quasi-quotations are not supported")
    is Quotation -> Quotation(quoted.unquoteAfterCommas(scope))
    else              -> this
}

private fun apply(proc: Procedure, arguments: List<SExpr>, scope: RuntimeScope): SExpr {
    if (proc.arity != VARARG) ensure(arguments.size == proc.arity) {
        "Arity mismatch: ${display(proc)} was called with ${arguments.size} arguments"
    }
    return if (proc.isMacro) proc.call(arguments, scope).evaluate(scope)
    else proc.call(arguments.map { it.evaluate(scope) }, scope)
}

private data class BuiltinSyntax(
    val name: String,
    val parameters: Int,
    val definition: (List<SExpr>, RuntimeScope) -> SExpr
)

val SExpr.isValue
    get() = when (this) {
        is Symbol, is Pair, Nil, is QuasiQuotation, is Unquote -> false
        is Literal<*>, is Quotation, is Procedure, is NormalizedSExpr -> true
    }

private val syntax = listOf(
    BuiltinSyntax("if", 3) { (cond, then, `else`), scope ->
        if (truthy(cond.evaluate(scope))) then else `else`
    },
    BuiltinSyntax("let", 3) { (sym, value, body), _ ->
        ensure(sym is Symbol)
        ensure(value.isValue) { "some values are not evaluated" }
        body.substitute(mapOf(sym.name to value))
    },
    BuiltinSyntax("lambda", 3) { (parameters, body), _ ->
        ensure(parameters.isList())
        quote(list("lambda".s, parameters, body))
    },
    BuiltinSyntax("eval", 1) { (expr), _ ->
        ensure(expr.isValue)
        expr.unwrap()
    }
).associateBy { it.name }

fun SExpr.reduce(scope: RuntimeScope): SExpr = when (this) {
    is NormalizedSExpr -> this
    is Symbol -> scope.get(name) ?: fail("unbound variable $name")
    is QuasiQuotation -> quoted.reduceQuasiQuotation()
    is Unquote -> fail("comma is illegal outside of quasi-quotation")
    is Pair -> run {
        ensure(isList()) { "bad syntax" }
        val arguments = cdr.extractList()
        when (val c = car.unwrap()) {
            is Symbol ->
                if (c.name in syntax) syntax.getValue(c.name).definition(arguments, scope)
                else applyFunction(scope.get(c.name)?.unwrap() ?: fail("unbound variable ${c.name}"), arguments, scope)
            else      -> applyFunction(c, arguments, scope)
        }
    }
    is Quotation -> normalized(quoted)
    is Nil -> fail("Illegal empty application ()")
    is Literal<*>, is Procedure -> this
}

private fun SExpr.reduceQuasiQuotation(): SExpr = when (this) {
    is Literal<*> -> this
    is Pair -> Pair("list".s, list(extractList().map { it.reduceQuasiQuotation() }))
    is QuasiQuotation -> fail("Nested quasi-quotation is illegal")
    is Unquote -> expr
    is NormalizedSExpr -> quote(expr)
    is Symbol, is Nil, is Quotation -> quote(this)
    is Procedure -> fail("cannot happen")
}

private fun SExpr.substitute(substitution: Map<String, SExpr>): SExpr = when (this) {
    is Symbol -> substitution[name] ?: this
    is Pair -> Pair(
        car.substitute(substitution),
        cdr.substitute(substitution)
    )
    is QuasiQuotation -> QuasiQuotation(
        quoted.substituteInsideQuasiQuotation(
            substitution
        )
    )
    is Unquote -> fail("Unquote is illegal outside of quasi-quotation")
    is Procedure, is Literal<*>, is Quotation, is Nil, is NormalizedSExpr -> this
}

private fun SExpr.substituteInsideQuasiQuotation(substitution: Map<String, SExpr>): SExpr = when (this) {
    is Pair -> Pair(
        car.substituteInsideQuasiQuotation(substitution),
        cdr.substituteInsideQuasiQuotation(substitution)
    )
    is QuasiQuotation -> fail("Nested quasi-quotation is illegal")
    is Unquote -> Unquote(expr.substitute(substitution))
    is Procedure -> fail("cannot happen")
    else              -> this
}

private fun applyFunction(proc: SExpr, arguments: List<SExpr>, scope: RuntimeScope): SExpr {
    if (arguments.any { !it.isValue }) fail("Not all arguments are evaluated")
    return when {
        proc.isList()   -> {
            val lst = proc.extractList()
            ensure(lst.size == 3)
            ensure(lst[0] == quote(Symbol("lambda")))
            ensure(lst[1].isList())
            val parameters = lst[1].symbolList()
            val body = lst[2].unquote()
            if (parameters.size != arguments.size) fail("Arity mismatch")
            body.substitute(parameters.zip(arguments).toMap())
        }
        proc is Closure -> {
            if (proc.arity != VARARG && proc.arity != arguments.size) fail("Arity mismatch")
            val subst = proc.parameters.zip(arguments).toMap()
            proc.body.substitute(subst)
        }
        proc is Builtin -> {
            if (proc.arity != VARARG && proc.arity != arguments.size) fail("Arity mismatch")
            normalized(proc.call(arguments, scope))
        }
        else            -> fail("Unknown type of procedure")
    }
}