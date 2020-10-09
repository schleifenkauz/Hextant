/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.lisp.SExpr

abstract class SpecialSyntax<E : SExprEditor<*>>(val name: String, val arity: Int) {
    abstract fun representsEditors(editors: List<SExprEditor<*>?>): Boolean

    abstract fun represents(expressions: List<SExpr>): Boolean

    abstract fun representEditors(context: Context, editors: List<SExprEditor<*>?>): E

    abstract fun represent(context: Context, expressions: List<SExpr>): E

    abstract fun desugar(editor: E): SExprEditor<*>

    abstract fun createTemplate(context: Context): E

    companion object {
        private val byName = mutableMapOf<String, SpecialSyntax<*>>()

        fun register(syntax: SpecialSyntax<*>) {
            byName[syntax.name] = syntax
        }

        fun unregister(syntax: SpecialSyntax<*>) {
            checkNotNull(byName.remove(syntax.name)) { "$syntax was not registered before" }
        }

        fun get(name: String) = byName[name]
    }
}