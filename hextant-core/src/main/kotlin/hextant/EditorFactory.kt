/**
 *@author Nikolaus Knop
 */

package hextant

import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import hextant.impl.myLogger
import hextant.util.ClassMap
import kotlin.reflect.KClass

/**
 * Used to resolve and register editables for edited objects
 */
interface EditorFactory {
    /**
     * Register the [factory] for the [editedCls],
     * such that for any call of `getEditor(editedCls)` this [EditorFactory] uses the specified [factory],
     * where `editedCls` denotes the specified [editedCls] or any superclasses,
     * unless another factory has been registered.
     */
    fun <T : Any> register(editedCls: KClass<T>, factory: (Context) -> Editor<T>)

    /**
     * Register the [factory] for the [editedCls],
     * such that for any call of getEditor(edited) this [EditorFactory] uses the specified [factory],
     * where `edited` denotes an instance of exactly [editedCls] not a sub- or superclass instance,
     * unless another factory has been registered.
     */
    fun <T : Any> register(editedCls: KClass<T>, factory: (Context, T) -> Editor<T>)

    /**
     * Tries to find a factory registered with [register]
     */
    fun <T : Any> getEditor(editedCls: KClass<T>, context: Context): Editor<T>

    /**
     * Tries to find a factory registered with [register]
     */
    fun <T : Any> getEditor(edited: T, context: Context): Editor<T>

    /**
     * The Editor factory property
     */
    companion object : Property<EditorFactory, Public, Internal>("Editor factory") {
        /**
         * @return a new [EditorFactory]
         */
        fun newInstance(): EditorFactory = Impl()

        val logger by myLogger()
    }

    @Suppress("UNCHECKED_CAST")
    private class Impl : EditorFactory {
        private val oneArgFactories = ClassMap.invariant<(Context, Any) -> Editor<Any>>()

        override fun <T : Any> register(editedCls: KClass<T>, factory: (Context, T) -> Editor<T>) {
            logger.config { "register factory for $editedCls" }
            oneArgFactories[editedCls] = factory as (Context, Any) -> Editor<Any>
        }

        @Synchronized override fun <T : Any> getEditor(edited: T, context: Context): Editor<T> {
            val editedCls = edited::class
            val factory = oneArgFactories[editedCls]
            if (factory != null) return factory(context, edited) as Editor<T>
            else {
                val msg = "No one-arg factory found for ${edited.javaClass}"
                logger.severe(msg)
                throw NoSuchElementException(msg)
            }
        }

        private val noArgFactories = ClassMap.contravariant<(Context) -> Editor<*>>()

        override fun <T : Any> register(editedCls: KClass<T>, factory: (Context) -> Editor<T>) {
            noArgFactories[editedCls] = factory
        }

        @Synchronized override fun <T : Any> getEditor(
            editedCls: KClass<T>,
            context: Context
        ): Editor<T> {
            val factory = noArgFactories[editedCls]
            if (factory != null) return factory(context) as Editor<T>
            throw NoSuchElementException("No no-arg factory found for $editedCls")
        }
    }
}

