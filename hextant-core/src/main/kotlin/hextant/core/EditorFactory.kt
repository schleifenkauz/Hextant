/**
 * @author Nikolaus Knop
 */

package hextant.core

import hextant.*
import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import hextant.core.editable.ConvertedEditable
import hextant.impl.myLogger
import hextant.util.ClassMap
import hextant.util.DoubleWeakHashMap
import kotlin.reflect.KClass

/**
 * Used to register and resolve [Editor]'s
 * * Editors produced by this factory are always "weakly-cached"
 *   Which means they are stored using a [java.lang.ref.WeakReference]
 *   and returned on further invocations unless they have been garbage collected
 */
interface EditorFactory {
    /**
     * Register the specified [factory] for the [editableCls]
     */
    fun <E : Editable<*>, Ed : Editor<E>> register(editableCls: KClass<E>, factory: (E, Context) -> Ed)

    /**
     * @return a maybe cached [Editor] for the specified [editable]
     */
    fun <E : Editable<*>> getEditor(editable: E, context: Context): Editor<E>

    @Suppress("UNCHECKED_CAST")
    private class Impl : EditorFactory {
        private val factories =
            ClassMap.invariant<(Editable<*>, Context) -> Editor<*>>()

        private val cache = DoubleWeakHashMap<Editable<*>, Editor<*>>()

        override fun <E : Editable<*>, Ed : Editor<E>> register(editableCls: KClass<E>, factory: (E, Context) -> Ed) {
            factories[editableCls] = factory as (Editable<*>, Context) -> Editor<*>
            logger.config { "Registered editor factory for $editableCls" }
        }

        override fun <E : Editable<*>> getEditor(editable: E, context: Context): Editor<E> {
            if (editable is ConvertedEditable<*, *>) return getEditor(editable.source, context) as Editor<E>
            val cached = cache[editable]
            if (cached != null) {
                logger.info { "Using cached ${cached.javaClass}" }
                return cached as Editor<E>
            }
            val cls = editable::class
            val factory = factories[cls]
            if (factory == null) {
                val msg = "No editor found for $cls"
                logger.severe(msg)
                throw NoSuchElementException(msg)
            }
            val new = factory(editable, context)
            logger.info { "Created new ${new.javaClass}" }
            cache[editable] = new
            return new as Editor<E>
        }
    }

    companion object : Property<EditorFactory, Public, Internal>("editor factory") {
        val logger by myLogger()
        fun newInstance(): EditorFactory = Impl()
    }
}

inline fun <reified E : Editable<*>, reified Ed : Editor<E>> EditorFactory.register(
    noinline factory: (E, Context) -> Ed
) {
    register(E::class, factory)
}
