/**
 *@author Nikolaus Knop
 */

package hextant.lispy

class Env(private val parent: Env?) {
    private val values = mutableMapOf<String, SExpr>()

    fun define(name: String, value: SExpr) {
        values[name] = value
    }

    fun set(name: String, value: SExpr) {
        val e = findDefiningEnv(name) ?: fail("cannot set variable $name because it was not defined")
        e.values[name] = value
    }

    fun get(name: String): SExpr =
        findDefiningEnv(name)?.values?.getValue(name) ?: fail("unbound variable $name")

    private fun findDefiningEnv(name: String): Env? = if (name in values) this else parent?.findDefiningEnv(name)

    fun child() = Env(this)

    companion object {
        fun root() = Env(null).apply {
            for ((name, builtin) in Builtin) define(name, builtin)
            for ((name, predefined) in Predefined) define(name, evaluate(predefined, this))
        }
    }
}