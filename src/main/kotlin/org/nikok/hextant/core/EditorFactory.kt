/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core

import org.nikok.hextant.Editable
import org.nikok.hextant.Editor
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.editable.Expandable
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
    private class Impl(private val expanderFactory: ExpanderFactory) : EditorFactory {
        private val factories =
                mutableMapOf<KClass<*>, MutableMap<KClass<*>, (Editable<*>) -> Editor<*>>>()

        private val cache = IdentityHashMap<Editable<*>, Editor<*>>()

        override fun <E : Editable<*>, Ed : Editor<E>> register(
            editableCls: KClass<E>,
            editorCls: KClass<Ed>,
            factory: (E) -> Ed
        ) {
            getFactories(editableCls).let {
                it[editorCls] = factory as (Editable<*>) -> Editor<*>
            }
            logger.config { "Registered editor factory for $editableCls" }
        }

        private fun <E : Editable<*>> getFactories(editableCls: KClass<E>) =
                factories.getOrPut(editableCls) { mutableMapOf() }

        override fun <E : Editable<*>, Ed : Editor<E>> getEditor(editorCls: KClass<Ed>, editable: E): Ed =
                if (editable is Expandable<*, *>) expanderFactory.getExpander(editable) as Ed
                else cache.getOrPut(editable) {
                    val cls = editable::class
                    val factory =
                            getFactories(cls).getOrPut(editorCls) {
                                resolveDefaultEditor(cls, editorCls) as ((Editable<*>) -> Editor<*>)?
                                ?: throw NoSuchElementException("No constructor found for $cls")
                            }
                    factory(editable)
                }.let { editorCls.cast(it) }

        companion object {
            private fun <E : Editable<*>, Ed : Editor<E>> resolveDefaultEditor(
                cls: KClass<out E>,
                editorCls: KClass<Ed>
            ): ((E) -> Ed)? {
                val constructor = editorCls.constructors
                                          .find {
                                              it.parameters.count { p -> !p.isOptional } == 1 &&
                                              it.parameters.first().type.isSupertypeOf(cls.starProjectedType)
                                          } ?: return null
                return { editable -> constructor.callBy(mapOf(constructor.parameters.first() to editable)) }
            }
        }
    }

    companion object : Property<EditorFactory, Public, Internal>("editor factory") {
        val logger = Logger.getLogger(EditorFactory::class.qualifiedName)

        fun newInstance(expanderFactory: ExpanderFactory): EditorFactory =
                Impl(expanderFactory)
    }
}

inline fun <reified E : Editable<*>, reified Ed : Editor<E>> EditorFactory.register(noinline factory: (E) -> Ed) {
    register(E::class, Ed::class, factory)
}

inline fun <E : Editable<*>, reified Ed : Editor<E>> EditorFactory.getEditor(editable: E) =
        getEditor(Ed::class, editable)
