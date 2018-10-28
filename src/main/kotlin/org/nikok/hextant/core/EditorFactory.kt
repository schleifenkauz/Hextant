/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core

import org.nikok.hextant.Editable
import org.nikok.hextant.Editor
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.prop.Property
import java.util.*
import java.util.logging.Logger
import kotlin.NoSuchElementException
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

interface EditorFactory {
    fun <E : Editable<*>, Ed : Editor<E>> register(editableCls: KClass<E>, editorCls: KClass<Ed>, factory: (E) -> Ed)

    fun <E : Editable<*>, Ed : Editor<E>> getEditor(editorCls: KClass<Ed>, editable: E): Ed

    @Suppress("UNCHECKED_CAST")
    private class Impl : EditorFactory {
        private val factories =
                mutableMapOf<KClass<*>, MutableMap<KClass<*>, (Editable<*>) -> Editor<*>>>()

        private val cache = IdentityHashMap<Editable<*>, Editor<*>>()

        override fun <E : Editable<*>, Ed : Editor<E>> register(
            editableCls: KClass<E>,
            editorCls: KClass<Ed>,
            factory: (E) -> Ed
        ) {
            factories.getOrPut(editableCls) { mutableMapOf() }.let {
                it[editorCls] = factory as (Editable<*>) -> Editor<*>
            }
            logger.config { "Registered editor factory for $editableCls" }
        }

        override fun <E : Editable<*>, Ed : Editor<E>> getEditor(editorCls: KClass<Ed>, editable: E): Ed =
                cache.getOrPut(editable) {
                    val cls = editable::class
                    val factory =
                            factories[cls]?.get(editorCls)
                            ?: throw NoSuchElementException("No factory registered fo $cls")
                    factory(editable)
                }.let { editorCls.cast(it) }
    }

    companion object : Property<EditorFactory, Public, Internal>("editor factory") {
        val logger = Logger.getLogger(EditorFactory::class.qualifiedName)

        fun newInstance(): EditorFactory = Impl()
    }
}

inline fun <reified E : Editable<*>, reified Ed: Editor<E>> EditorFactory.register(noinline factory: (E) -> Ed) {
    register(E::class, Ed::class, factory)
}