/**
 * @author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.lisp.rt.RuntimeScope

interface RuntimeScopeAware {
    val inheritScope: Boolean

    var scope: RuntimeScope

    fun initializeScope(parent: RuntimeScopeAware?)

    private class Impl(override val inheritScope: Boolean) : RuntimeScopeAware {
        override lateinit var scope: RuntimeScope

        override fun initializeScope(parent: RuntimeScopeAware?) {
            scope = when {
                parent == null      -> RuntimeScope.root()
                parent.inheritScope -> parent.scope.child()
                else                -> RuntimeScope.empty()
            }
        }
    }

    companion object {
        fun delegate(inheritScope: Boolean = true): RuntimeScopeAware = Impl(inheritScope)
    }
}