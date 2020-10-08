/**
 *@author Nikolaus Knop
 */

package hextant.lisp.rt

import bundles.SimpleProperty
import hextant.lisp.SExpr

class RuntimeScope(
    private val parent: RuntimeScope?,
    private val values: MutableMap<String, SExpr> = mutableMapOf<String, SExpr>()
) {
    val boundVariables: Collection<Map.Entry<String, SExpr>> get() = values.entries

    val stack: Sequence<RuntimeScope> = generateSequence(this) { it.parent }

    fun define(name: String, value: SExpr) {
        values[name] = value
    }

    fun set(name: String, value: SExpr) {
        val e = findDefiningEnv(name) ?: fail("cannot set variable $name before its definition")
        e.values[name] = value
    }

    fun get(name: String): SExpr =
        findDefiningEnv(name)?.values?.getValue(name) ?: fail("unbound variable $name")

    private fun findDefiningEnv(name: String): RuntimeScope? =
        if (name in values) this else parent?.findDefiningEnv(name)

    fun child() = RuntimeScope(this)

    companion object : SimpleProperty<RuntimeScope>("runtime scope") {
        fun root() = RuntimeScope(null).apply {
            registerBuiltins()
            loadPrelude()
        }

        fun empty() = RuntimeScope(null)

        fun withBindings(bindings: Map<String, SExpr>) = RuntimeScope(null, bindings.toMutableMap())
    }
}