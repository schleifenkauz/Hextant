/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core

import org.nikok.hextant.Editable
import org.nikok.hextant.Editor
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.prop.Property
import java.lang.reflect.Constructor
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
                            ?: resolveDefaultEditor(cls)
                            ?: throw NoSuchElementException("No factory registered fo $cls")
                    factory(editable)
                }.let { editorCls.cast(it) }

        companion object {
            private fun <E : Editable<*>> resolveDefaultEditor(cls: KClass<out E>): ((E) -> Editor<E>)? {
                val qualifiedName = cls.qualifiedName ?: return null
                val clsName = cls.simpleName ?: return null
                val pkg = qualifiedName.removeSuffix(clsName)
                val editorClsName = clsName.removeSuffix("Editable") + "Editor"
                val editorPkg = pkg.removeSuffix("editable.") + "editor."
                val editorCls = Class.forName("$editorPkg$editorClsName")
                if (!Editor::class.java.isAssignableFrom(editorCls)) return null
                val constructor = editorCls.constructors
                                          .find {
                                              it.isAccessible &&
                                              it.parameterCount == 1 &&
                                              it.parameters.first().type.isAssignableFrom(cls.java)
                                          } ?: return null
                constructor as Constructor<Editor<E>>
                return { editable -> constructor.newInstance(editable) }
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