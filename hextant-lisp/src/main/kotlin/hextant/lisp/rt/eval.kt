/**
 * @author Nikolaus Knop
 */

package hextant.lisp.rt

import hextant.lisp.*

fun SExpr.evaluate(scope: RuntimeScope = RuntimeScope.root()): SExpr = when (this) {
    is Symbol -> scope.get(name)
    is Quotation -> quoted
    is QuasiQuotation -> quoted.unquoteAfterCommas(scope)
    is Unquote -> fail("comma is illegal outside of quasi-quotation")
    is Pair -> {
        ensure(isList())
        val proc = car.evaluate(scope)
        ensure(proc is Procedure) { "${display(proc)} is not a procedure" }
        val args = cdr.extractList()
        apply(proc, args, scope)
    }
    is Nil -> fail("Illegal empty application ()")
    is Literal<*>, is Procedure -> this
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
    val definition: (List<SExpr>, RuntimeScope) -> kotlin.Pair<SExpr, RuntimeScope>
)

private val syntax = listOf(
    BuiltinSyntax("if", 3) { (cond, then, `else`), scope ->
        if (truthy(cond.evaluate(scope))) then to scope else `else` to scope
    },
    BuiltinSyntax("let", 3) { (sym, value, body), scope ->
        ensure(sym is Symbol)
        val sc = scope.child()
        sc.define(sym.name, value.evaluate(scope))
        body to sc
    },
    BuiltinSyntax("lambda", 3) { (parameters, body), scope ->
        ensure(parameters.isList())
        val params = parameters.extractList()
        val names = params.map { it as? Symbol ?: fail("bad syntax") }.map { it.name }
        Closure(null, names, body, false, scope) to scope
    }
).associateBy { it.name }

fun SExpr.reduce(scope: RuntimeScope = RuntimeScope.root()): kotlin.Pair<SExpr, RuntimeScope> =
    when (this) {
        is Symbol -> scope.get(name) to scope
        is Quotation -> quoted to RuntimeScope.empty()
        is QuasiQuotation -> TODO()
        is Unquote -> fail("comma is illegal outside of quasi-quotation")
        is Pair -> run {
            ensure(isList()) { "bad syntax" }
            val arguments = cdr.extractList()
            when (val c = car) {
                is Symbol ->
                    if (c.name in syntax) syntax.getValue(c.name).definition(arguments, scope)
                    else substitute(scope.get(c.name), arguments, scope)
                else      -> substitute(c, arguments, scope)
            }
        }
        is Nil -> fail("Illegal empty application ()")
        is Literal<*>, is Procedure -> this to RuntimeScope.empty()
    }

private fun substitute(
    proc: SExpr,
    arguments: List<SExpr>,
    scope: RuntimeScope
): kotlin.Pair<SExpr, RuntimeScope> {
    ensure(proc is Procedure) { "Not a procedure" }
    if (proc.arity != VARARG && proc.arity != arguments.size) fail("Arity mismatch")
    return when (proc) {
        is Closure -> {
            val sc = proc.closureScope.child()
            for ((p, a) in proc.parameters.zip(arguments.map { it.evaluate(scope) })) {
                sc.define(p, a)
            }
            proc.body to sc
        }
        is Builtin -> proc.call(arguments, scope) to RuntimeScope.empty()
        else       -> fail("Unknown type of procedure")
    }
}