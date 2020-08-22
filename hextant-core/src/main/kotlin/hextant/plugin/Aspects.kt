/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.plugin

import bundles.Property
import hextant.context.Internal
import hextant.plugins.Implementation
import kollektion.ClassMap
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf

class Aspects {
    private val implementations = mutableMapOf<KClass<*>, ClassMap<Any>>()

    fun addImplementation(implementation: Implementation) {
        val aspect = getClass(implementation.aspect) as KClass<Any>
        val case = getClass(implementation.feature)
        val impl = getClass(implementation.clazz)
        check(impl.isSubclassOf(aspect)) { "invalid implementation class $impl for aspect $aspect" }
        val instance = impl.objectInstance ?: impl.createInstance()
        implement(aspect, case, instance)
    }

    fun <A : Any> implement(aspect: KClass<A>, case: KClass<*>, implementation: A) {
        implementations(aspect)[case] = implementation
    }

    private fun getClass(name: String) = Class.forName(name).kotlin

    private fun <A : Any> implementations(aspect: KClass<A>) =
        implementations.getOrPut(aspect) { ClassMap.contravariant() }


    fun <A : Any> get(aspect: KClass<A>, case: KClass<*>): A =
        implementations(aspect)[case] as? A? ?: error("No implementation of aspect ${aspect.simpleName} for $case")

    inline fun <reified A : Any> get(obj: Any): A = get(A::class, obj::class)

    inline fun <T : Any, reified A : Any, R> T.call(func: A.(Aspects, T) -> R): R {
        val impl = get(A::class, this::class)
        return impl.func(this@Aspects, this)
    }

    inline fun <T : Any, reified A : Any, P, R> T.call(func: A.(T, P) -> R, parameter: P): R {
        val impl = get(A::class, this::class)
        return impl.func(this, parameter)
    }

    inline operator fun <T : Any, reified A : Any, R> (A.(Aspects, T) -> R).invoke(obj: T): R = obj.call(this)

    companion object : Property<Aspects, Any, Internal>("aspects")
}
