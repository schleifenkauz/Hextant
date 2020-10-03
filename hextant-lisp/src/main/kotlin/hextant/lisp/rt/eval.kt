/**
 * @author Nikolaus Knop
 */

package hextant.lisp.rt

import hextant.lisp.*

fun SExpr.evaluate(env: Env = Env.root()): SExpr = when (this) {
    is Symbol -> env.get(name)
    is Quotation -> quoted
    is QuasiQuotation -> quoted.unquoteAfterCommas(env)
    is Unquote -> fail("comma is illegal outside of quasi-quotation")
    is Pair -> {
        val proc = car.evaluate(env)
        ensure(proc is Procedure) { "${display(proc)} is not a procedure" }
        val args = cdr.extractList()
        apply(proc, args, env)
    }
    is Nil -> fail("Illegal empty application ()")
    is Literal<*>, is Procedure -> this
}

private fun SExpr.unquoteAfterCommas(env: Env): SExpr = when (this) {
    is Pair -> Pair(car.unquoteAfterCommas(env), cdr.unquoteAfterCommas(env))
    is Unquote -> expr.evaluate(env)
    is QuasiQuotation -> fail("Nested quasi-quotations are not supported")
    is Quotation -> Quotation(quoted.unquoteAfterCommas(env))
    else              -> this
}

private fun apply(proc: Procedure, arguments: List<SExpr>, env: Env): SExpr {
    if (proc.arity != VARARG) ensure(arguments.size == proc.arity) {
        "Arity mismatch: ${display(proc)} was called with ${arguments.size} arguments"
    }
    return if (proc.isMacro) proc.call(arguments, env).evaluate(env)
    else proc.call(arguments.map { it.evaluate(env) }, env)
}