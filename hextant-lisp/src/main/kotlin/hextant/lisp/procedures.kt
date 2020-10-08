package hextant.lisp

import hextant.lisp.rt.RuntimeScope
import hextant.lisp.rt.evaluate

data class Builtin(
    override val name: String,
    override val arity: Int,
    override val isMacro: Boolean,
    private val def: (arguments: List<SExpr>, callerScope: RuntimeScope) -> SExpr
) : Procedure() {
    override fun call(arguments: List<SExpr>, callerScope: RuntimeScope): SExpr = def(arguments, callerScope)
}

data class Closure(
    override val name: String?,
    val parameters: List<String>,
    val body: SExpr,
    override val isMacro: Boolean,
    val closureScope: RuntimeScope
) : Procedure() {
    override val arity: Int
        get() = parameters.size

    override fun call(arguments: List<SExpr>, callerScope: RuntimeScope): SExpr {
        val callEnv = closureScope.child()
        for ((name, value) in parameters.zip(arguments)) callEnv.define(name, value)
        return body.evaluate(callEnv)
    }
}

interface E : I {
    fun f(x: Int) {
        init(x)
    }
}

interface I {
    fun init(x: Int)
}

class D : I {
    override fun init(x: Int) {
        println(x)
    }
}

class X : I by D()