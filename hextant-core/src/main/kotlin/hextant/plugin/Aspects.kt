/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.plugin

import bundles.Property
import hextant.context.Internal
import kollektion.ClassMap
import kotlin.reflect.KClass

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

    companion object : Property<Aspects, Any, Internal>("aspects")
}
