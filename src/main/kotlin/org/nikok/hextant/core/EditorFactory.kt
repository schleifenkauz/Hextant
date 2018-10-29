/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core

import org.nikok.hextant.Editable
import org.nikok.hextant.Editor
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.editable.Expandable
import org.nikok.hextant.core.editor.Expander
import org.nikok.hextant.prop.Property
import java.util.*
import java.util.logging.Logger
import kotlin.NoSuchElementException
import kotlin.reflect.KClass
import kotlin.reflect.full.*

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
                            ?: resolveDefaultEditor(cls, editorCls)
                            ?: throw NoSuchElementException("No editor-factory registered fo $cls")
                    factory(editable)
                }.let { editorCls.cast(it) }

        companion object {
            private fun <E : Editable<*>, Ed : Editor<E>> resolveDefaultEditor(
                cls: KClass<out E>,
                editorCls: KClass<Ed>
            ): ((E) -> Ed)? {
                val constructor = editorCls.constructors
                                          .find {
                                              it.parameters.size == 1 &&
                                              it.parameters.first().type.isSupertypeOf(cls.starProjectedType)
                                          } ?: return null
                return { editable -> constructor.call(editable) }
            }
        }
    }

    companion object : Property<EditorFactory, Public, Internal>("editor factory") {
        val logger = Logger.getLogger(EditorFactory::class.qualifiedName)

        fun newInstance(): EditorFactory = Impl()
    }
}

inline fun <reified E : Editable<*>, reified Ed : Editor<E>> EditorFactory.register(noinline factory: (E) -> Ed) {
    register(E::class, Ed::class, factory)
}

inline fun <E : Editable<*>, reified Ed : Editor<E>> EditorFactory.getEditor(editable: E) =
        getEditor(Ed::class, editable)

inline fun <E : Editable<*>, reified Ex : Expander<E>> EditorFactory.getExpander(expandable: Expandable<*, E>): Ex {
    return getEditor(expandable)
}

inline fun <Ed : Editable<*>, reified E: Expandable<*, Ed>, reified Ex : Expander<Ed>> EditorFactory.registerExpander(noinline factory: (E) -> Ex) {
    @Suppress("UNCHECKED_CAST")
    register(E::class, Ex::class as KClass<Editor<E>>, factory as (E) -> Editor<E>)
}