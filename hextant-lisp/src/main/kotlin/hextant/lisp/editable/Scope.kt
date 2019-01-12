/**
 * @author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.lisp.Identifier
import hextant.lisp.SExpr
import org.nikok.reaktive.Observer
import org.nikok.reaktive.value.*
import org.nikok.reaktive.value.binding.orElse

class Scope(private val parent: Scope? = null) {
    private val boundIdentifiers = mutableMapOf<Identifier, SExpr?>()

    private val queried = hextant.util.WeakValuesMap<Identifier, ReactiveVariable<SExpr?>>()

    private val parentBinders = mutableMapOf<Identifier, Observer>()

    fun resolve(identifier: Identifier): ReactiveValue<SExpr?> {
        val expr = boundIdentifiers[identifier]
        val queried = queried.getOrPut(identifier) {
            reactiveVariable("binding for $identifier", expr)
        }
        if (parent != null) return queried.orElse(parent.resolve(identifier))
        return queried
    }

    fun register(identifier: Identifier, expr: SExpr?) {
        boundIdentifiers[identifier] = expr
        queried[identifier]?.run {
            parentBinders[identifier]?.kill()
            set(expr)
        }
    }

    fun unregister(identifier: Identifier) {
        boundIdentifiers.remove(identifier)
        val alternative = parent?.resolve(identifier) ?: reactiveValue("always null", null)
        val parentBinder = queried[identifier]?.bind(alternative)
        if (parentBinder != null) {
            parentBinders[identifier] = parentBinder
        }
    }
}