/**
 * @author Nikolaus Knop
 */

package hextant.lispy.rt

import hextant.lispy.*

fun SExpr.evaluate(env: Env = Env.root()): SExpr = when (this) {
    is Symbol -> env.get(name)
    is Quoted -> expr
    is Pair -> {
        val proc = car.evaluate(env)
        ensure(proc is Procedure) { "${display(proc)} is not a procedure" }
        val args = cdr.extractList()
        apply(proc, args, env)
    }
    else      -> this
}

fun apply(proc: Procedure, arguments: List<SExpr>, env: Env): SExpr {
    if (proc.arity != VARARG) ensure(arguments.size == proc.arity) {
        "Arity mismatch: ${display(proc)} was called with ${arguments.size} arguments"
    }
    return if (proc.isMacro) proc.call(arguments, env).evaluate(env)
    else proc.call(arguments.map { it.evaluate(env) }, env)
}