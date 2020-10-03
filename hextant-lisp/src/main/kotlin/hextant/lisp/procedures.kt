package hextant.lisp

import hextant.lisp.rt.Env
import hextant.lisp.rt.evaluate

data class Builtin(
    override val name: String,
    override val arity: Int,
    override val isMacro: Boolean,
    private val def: (arguments: List<SExpr>, callerEnv: Env) -> SExpr
) : Procedure() {
    override fun call(arguments: List<SExpr>, callerEnv: Env): SExpr = def(arguments, callerEnv)
}

data class Closure(
    override val name: String?,
    val parameters: List<String>,
    val body: SExpr,
    override val isMacro: Boolean,
    val closureEnv: Env
) : Procedure() {
    override val arity: Int
        get() = parameters.size

    override fun call(arguments: List<SExpr>, callerEnv: Env): SExpr {
        val callEnv = closureEnv.child()
        for ((name, value) in parameters.zip(arguments)) callEnv.define(name, value)
        return body.evaluate(callEnv)
    }
}