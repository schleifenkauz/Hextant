/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core

import org.nikok.hextant.Editable
import org.nikok.hextant.Editor
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.impl.ClassMap
import org.nikok.hextant.prop.Property
import java.util.*
import java.util.logging.Logger
import kotlin.NoSuchElementException
import kotlin.reflect.KClass

interface EditorFactory {
    fun <E : Editable<*>> register(editableCls: KClass<E>, factory: (E) -> Editor<E>)

    fun <E : Editable<*>> getEditor(editable: E): Editor<E>

    @Suppress("UNCHECKED_CAST")
    private class Impl : EditorFactory {
        private val factories = ClassMap.covariant<(Editable<*>) -> Editor<*>>()

        private val cache = IdentityHashMap<Editable<*>, Editor<*>>()

        override fun <E : Editable<*>> register(editableCls: KClass<E>, factory: (E) -> Editor<E>) {
            logger.config { "Registered editor factory for $editableCls" }
            factories[editableCls] = factory as (Editable<*>) -> Editor<*>
        }

        override fun <E : Editable<*>> getEditor(editable: E): Editor<E> =
                cache.getOrPut(editable) {
                    val cls = editable::class
                    val factory = factories[cls] ?: throw NoSuchElementException("No factory registered fo $cls")
                    factory(editable)
                } as Editor<E>
    }

    companion object: Property<EditorFactory, Public, Internal>("editor factory") {
        val logger = Logger.getLogger(EditorFactory::class.qualifiedName)

        fun newInstance(): EditorFactory = Impl()
    }
}

inline fun <reified E : Editable<*>> EditorFactory.register(noinline factory: (E) -> Editor<E>) {
    register(E::class, factory)
}