/**
 *@author Nikolaus Knop
 */

package hextant.lisp.rt

import bundles.SimpleProperty
import hextant.codegen.UseEditor
import hextant.lisp.SExpr
import hextant.lisp.editor.RuntimeScopeEditor

@UseEditor(RuntimeScopeEditor::class)
class RuntimeScope private constructor(
    private val parent: RuntimeScope?,
    private val userInput: (RuntimeScope, String) -> SExpr?
) {
    private val values: MutableMap<String, SExpr> = mutableMapOf()

    fun define(name: String, value: SExpr) {
        values[name] = value
    }

    fun set(name: String, value: SExpr) {
        val e = findDefiningEnv(name) ?: fail("cannot set variable $name before its definition")
        e.values[name] = value
    }

    private fun getFromUser(name: String): SExpr? =
        userInput(this, name)?.also { define(name, it) } ?: parent?.getFromUser(name)

    fun get(name: String): SExpr? =
        findDefiningEnv(name)?.values?.getValue(name) ?: getFromUser(name)

    private fun findDefiningEnv(name: String): RuntimeScope? =
        if (name in values) this else parent?.findDefiningEnv(name)

    fun child() = RuntimeScope(this, noUserInput)

    companion object : SimpleProperty<RuntimeScope>("runtime scope") {
        private val noUserInput = { _: RuntimeScope, _: String -> null }

        fun root(userInput: (RuntimeScope, String) -> SExpr? = noUserInput) = RuntimeScope(null, userInput).apply {
            registerBuiltins()
            //            loadPrelude()
        }

        fun empty() = RuntimeScope(null, noUserInput)
    }
}