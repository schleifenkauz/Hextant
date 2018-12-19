/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core

import org.nikok.hextant.Editable
import org.nikok.hextant.Editor
import org.nikok.hextant.bundle.Property
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.impl.ClassMap
import org.nikok.hextant.core.impl.DoubleWeakHashMap
import java.util.logging.Logger
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
    fun <E : Editable<*>, Ed : Editor<E>> register(editableCls: KClass<E>, factory: (E) -> Ed)

    /**
     * @return a maybe cached [Editor] for the specified [editable]
     */
    fun <E : Editable<*>, Ed : Editor<E>> getEditor(editable: E): Ed

    @Suppress("UNCHECKED_CAST")
    private class Impl : EditorFactory {
        private val factories =
            ClassMap.invariant<(Editable<*>) -> Editor<*>>()

        private val cache = DoubleWeakHashMap<Editable<*>, Editor<*>>()

        override fun <E : Editable<*>, Ed : Editor<E>> register(
            editableCls: KClass<E>,
            factory: (E) -> Ed
        ) {
            factories[editableCls] = factory as (Editable<*>) -> Editor<*>
            logger.config { "Registered editor factory for $editableCls" }
        }

        override fun <E : Editable<*>, Ed : Editor<E>> getEditor(editable: E): Ed {
            val cached = cache[editable]
            if (cached != null) return cached as Ed
            val cls = editable::class
            val factory = factories[cls]
            if (factory == null) {
                val msg = "No editor found for $cls"
                logger.severe(msg)
                throw NoSuchElementException(msg)
            }
            return factory(editable) as Ed
        }
    }

    companion object : Property<EditorFactory, Public, Internal>("editor factory") {
        val logger = Logger.getLogger(EditorFactory::class.qualifiedName)
        fun newInstance(): EditorFactory = Impl()
    }
}

inline fun <reified E : Editable<*>, reified Ed : Editor<E>> EditorFactory.register(noinline factory: (E) -> Ed) {
    register(E::class, factory)
}
