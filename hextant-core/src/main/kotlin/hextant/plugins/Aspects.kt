/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.plugins

import bundles.Property
import bundles.property
import hextant.context.Internal
import kollektion.ClassMap
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf

/**
 * Objects of this class are used to manage the *implementations* of different *aspects* for certain *features*.
 */
class Aspects {
    private val implementations = mutableMapOf<KClass<*>, ClassMap<Any>>()

    /**
     * Provide the given [implementation] of the given [aspect] for the given [feature].
     *
     * Subsequent calls of `get(aspect, feature)` will return the provided [implementation].
     */
    fun <A : Any> implement(aspect: KClass<A>, feature: KClass<*>, implementation: A) {
        implementations(aspect)[feature] = implementation
    }

    /**
     * Implements the given [Implementation.aspect] of the specified [Implementation.feature] with an instance of the [Implementation.clazz].
     *
     * All classes are loaded with the given [classLoader].
     */
    fun addImplementation(implementation: Implementation, classLoader: ClassLoader) {
        val aspect = classLoader.loadClass(implementation.aspect).kotlin as KClass<Any>
        val case = classLoader.loadClass(implementation.feature).kotlin
        val impl = classLoader.loadClass(implementation.clazz).kotlin
        check(impl.isSubclassOf(aspect)) { "invalid implementation class $impl for aspect $aspect" }
        val instance = impl.objectInstance ?: impl.createInstance()
        implement(aspect, case, instance)
    }

    private fun <A : Any> implementations(aspect: KClass<A>) =
        implementations.getOrPut(aspect) { ClassMap.contravariant() }

    /**
     * Try to resolve an implementation of the given [aspect] for the specified [feature].
     *
     * @throws NoSuchElementException if no appropriate implementation is found.
     */
    fun <A : Any> get(aspect: KClass<A>, feature: KClass<*>): A =
        implementations(aspect)[feature] as? A?
            ?: throw NoSuchElementException("No implementation of aspect ${aspect.simpleName} for $feature")

    /**
     * Syntactic sugar for the [get]-method.
     */
    inline fun <reified A : Any> get(obj: Any): A = get(A::class, obj::class)

    companion object : Property<Aspects, Internal> by property("aspects")
}
