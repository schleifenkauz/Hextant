/**
 *@author Nikolaus Knop
 */

package hextant

import bundles.Property
import hextant.core.Internal
import kollektion.TypeMap
import kotlin.reflect.KType

/**
 * Used to resolve and register editor for specific result types.
 */
class EditorFactory private constructor() {
    private val noArgFactories = TypeMap.covariant<(Context) -> Editor<*>>()
    private val oneArgFactories = mutableMapOf<KType, (Context, Any?) -> Editor<Any?>>()

    /**
     * Register the [factory] for the [resultType],
     * such that for any call of `getEditor(type)` this [EditorFactory] uses the specified [factory],
     * where `type` denotes the specified [resultType] or of its supertypes,
     * unless another factory has been registered.
     */
    fun register(resultType: KType, factory: (Context) -> Editor<Any?>) {
        noArgFactories[resultType] = factory
    }

    /**
     * Register the [factory] for the [resultType],
     * such that for any call of getEditor(resultType, result) this [EditorFactory] uses the specified [factory],
     * where `result` denotes an instance of exactly [resultType] not a sub- or supertype instance,
     * unless another factory has been registered.
     */
    fun register(resultType: KType, factory: (Context, Any?) -> Editor<Any?>) {
        oneArgFactories[resultType] = factory
    }

    /**
     * Creates a new editor for results of the specified [resultType] using one of the registered factories.
     * @throws NoSuchElementException if no appropriate factory was registered.
     */
    fun createEditor(resultType: KType, context: Context): Editor<Any?> {
        val factory = noArgFactories[resultType]
        if (factory != null) return factory(context)
        throw NoSuchElementException("No no-arg factory found for $resultType")
    }

    /**
     * Creates a new editor which produces the given [result] using one of the registered factories.
     * @throws NoSuchElementException if no appropriate factory was registered.
     */
    fun <T> createEditor(type: KType, result: T, context: Context): Editor<T> {
        val factory = oneArgFactories[type]
        @Suppress("UNCHECKED_CAST")
        (if (factory != null) return factory(context, result) as Editor<T>
        else throw NoSuchElementException("No one-arg factory found for $type"))
    }

    /**
     * The Editor factory property
     */
    companion object : Property<EditorFactory, Any, Internal>("Editor factory") {
        /**
         * @return a new [EditorFactory]
         */
        fun newInstance(): EditorFactory = EditorFactory()
    }
}

