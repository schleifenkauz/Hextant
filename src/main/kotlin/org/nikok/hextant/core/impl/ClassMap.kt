/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.impl

import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

internal sealed class ClassMap<V> {
    abstract operator fun get(cls: KClass<*>): V?

    abstract operator fun set(cls: KClass<*>, value: V)

    inline fun <reified T : Any> get() = get(T::class)

    private class Invariant<V> : ClassMap<V>() {
        private val map = mutableMapOf<KClass<*>, V>()

        override fun get(cls: KClass<*>): V? {
            map[cls]?.let { return it }
            for (c in cls.allSuperclasses) {
                map[c]?.let { return it }
            }
            return null
        }

        override fun set(cls: KClass<*>, value: V) {
            map[cls] = value
        }
    }

    private class Contravariant<V>: ClassMap<V>() {
        private val map = mutableMapOf<KClass<*>, V>()

        override fun get(cls: KClass<*>): V? = map[cls]

        override fun set(cls: KClass<*>, value: V) {
            map[cls] = value
            for (c in cls.allSuperclasses) {
                map.putIfAbsent(c, value)
            }
        }
    }

    private class Covariant<V> : ClassMap<V>() {
        private val map = mutableMapOf<KClass<*>, V>()

        override fun get(cls: KClass<*>): V? = map[cls]

        override fun set(cls: KClass<*>, value: V) {
            map[cls] = value
        }
    }

    companion object {
        /**
         * @return A [ClassMap] where values are associated with the key class and all of its superclasses
         */
        fun <V> invariant(): ClassMap<V> = Invariant()

        /**
         * @return A [ClassMap] where values are associated with the key-class and all of its subclasses
         */
        fun <V> contravariant(): ClassMap<V> = Contravariant()

        /**
         * @return A [ClassMap] where values are associated with their exact key-class
         */
        fun <V> covariant(): ClassMap<V> = Covariant()
    }
}