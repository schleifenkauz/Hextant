/**
 *@author Nikolaus Knop
 */

package hextant.fx

import bundles.PublicProperty
import bundles.property
import hextant.core.view.EditorControl
import reaktive.Observer
import reaktive.observe
import reaktive.value.now
import validated.Validated
import validated.orNull
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

@PublishedApi internal class ResultStyleClasses {
    private val map = mutableMapOf<KClass<*>, (MutableCollection<(Any) -> String?>)>()

    fun <R : Any> register(cls: KClass<R>, style: (R) -> String?) {
        @Suppress("UNCHECKED_CAST")
        map.getOrPut(cls) { mutableSetOf() }.add(style as (Any) -> String?)
    }

    fun <R : Any> unregister(cls: KClass<R>, style: (R) -> String?) {
        map.getValue(cls).remove(style)
    }

    fun apply(control: EditorControl<*>): Observer {
        val result = control.target.result
        var last = getStyleClasses(result.now)
        control.root.styleClass.addAll(last)
        return result.observe(control) { _, _, new ->
            val next = getStyleClasses(new)
            val removed = last - next
            val added = next - last
            root.styleClass.removeAll(removed)
            root.styleClass.addAll(added)
            last = next
        }
    }

    private fun getStyleClasses(result: Validated<Any?>): Collection<String> {
        val value = result.orNull() ?: return emptyList()
        val superclasses = value::class.allSuperclasses + value::class
        return superclasses.mapNotNull { cls ->
            map[cls].orEmpty().firstNotNull { it(value) }
        }
    }

    companion object : PublicProperty<ResultStyleClasses> by property("result style") {
        private fun <E, F : Any> Iterable<E>.firstNotNull(f: (E) -> F?): F? {
            for (value in this) f(value)?.let { return it }
            return null
        }
    }
}