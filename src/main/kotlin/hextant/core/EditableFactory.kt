/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.core

import hextant.Editable
import hextant.bundle.Property
import hextant.core.CorePermissions.Internal
import hextant.core.CorePermissions.Public
import hextant.core.impl.ClassMap
import hextant.core.impl.myLogger
import kotlin.reflect.KClass

/**
 * Used to resolve and register editables for edited objects
 */
interface EditableFactory {
    /**
     * Register the [factory] for the [editedCls],
     * such that for any call of `getEditable(editedCls)` this [EditableFactory] uses the specified [factory],
     * where `editedCls` denotes the specified [editedCls] or any superclasses,
     * unless another factory has been registered.
     */
    fun <T : Any> register(editedCls: KClass<T>, factory: () -> Editable<T>)

    /**
     * Register the [factory] for the [editedCls],
     * such that for any call of getEditable(edited) this [EditableFactory] uses the specified [factory],
     * where `edited` denotes an instance of exactly [editedCls] not a sub- or superclass instance,
     * unless another factory has been registered.
     */
    fun <T : Any> register(editedCls: KClass<T>, factory: (T) -> Editable<T>)

    /**
     * Tries to find a factory registered with [register]
     */
    fun <T : Any> getEditable(editedCls: KClass<T>): Editable<T>

    /**
     * Tries to find a factory registered with [register]
     */
    fun <T : Any> getEditable(edited: T): Editable<T>

    /**
     * The editable factory property
     */
    companion object : Property<EditableFactory, Public, Internal>("editable factory") {
        /**
         * @return a new [EditableFactory]
         */
        fun newInstance(): EditableFactory = Impl()

        val logger by myLogger()
    }

    private class Impl : EditableFactory {
        private val oneArgFactories = ClassMap.covariant<(Any) -> Editable<Any>>()

        override fun <T : Any> register(editedCls: KClass<T>, factory: (T) -> Editable<T>) {
            logger.config { "register factory for $editedCls" }
            oneArgFactories[editedCls] = factory as (Any) -> Editable<Any>
        }

        override fun <T : Any> getEditable(edited: T): Editable<T> {
            val editedCls = edited::class
            val factory = oneArgFactories[editedCls]
            if (factory != null) return factory(edited) as Editable<T>
            else {
                val msg = "No one-arg factory found for ${edited.javaClass}"
                logger.severe(msg)
                throw NoSuchElementException(msg)
            }
        }

        private val noArgFactories = ClassMap.contravariant<() -> Editable<*>>()

        override fun <T : Any> register(editedCls: KClass<T>, factory: () -> Editable<T>) {
            noArgFactories[editedCls] = factory
        }

        override fun <T : Any> getEditable(
            editedCls: KClass<T>
        ): Editable<T> {
            val factory = noArgFactories[editedCls]
            if (factory != null) return factory() as Editable<T>
            throw NoSuchElementException("No no-arg factory found for $editedCls")
        }
    }
}

/**
 * Syntactic sugar for register(T::class, factory)
 */
inline fun <reified T : Any> EditableFactory.register(noinline factory: (T) -> Editable<T>) {
    register(T::class, factory)
}

/**
 * Syntactic sugar for register(T::class, factory)
 */
inline fun <reified T : Any> EditableFactory.register(noinline factory: () -> Editable<T>) {
    register(T::class, factory)
}