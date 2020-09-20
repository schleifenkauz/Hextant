/**
 *@author Nikolaus Knop
 */

package hextant.sample.rt

import hextant.sample.Identifier

class RuntimeContext private constructor(private val parent: RuntimeContext?) {
    private val variables = mutableMapOf<Identifier, Any>()

    fun get(name: Identifier): Any = variables[name] ?: parent?.get(name) ?: error("Variable not defined: '$name'")

    fun define(name: Identifier, value: Any) {
        check(name !in variables) { "Variable $name already defined" }
        variables[name] = value
    }

    fun assign(name: Identifier, value: Any) {
        var ctx = this
        while (true) {
            if (name in ctx.variables) {
                ctx.variables[name] = value
                break
            } else ctx = ctx.parent ?: break
        }
        error("Variable not defined: '$name'")
    }

    fun child() = RuntimeContext(this)

    companion object {
        fun root() = RuntimeContext(null)
    }
}