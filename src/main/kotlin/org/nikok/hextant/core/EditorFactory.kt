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
import sun.reflect.CallerSensitive
import sun.reflect.Reflection
import java.util.*
import java.util.logging.Logger
import kotlin.NoSuchElementException
import kotlin.reflect.KClass
import kotlin.reflect.full.*

interface EditorFactory {
    fun <E : Editable<*>, Ed : Editor<E>> register(editableCls: KClass<E>, editorCls: KClass<Ed>, factory: (E) -> Ed)

    fun <E : Editable<*>, Ed : Editor<E>> getEditor(editorCls: KClass<Ed>, editable: E): Ed

    fun <E : Editable<*>, Ed : Editor<E>> registerEditorClass(editableCls: KClass<E>, editorCls: KClass<Ed>)

    fun <E : Editable<*>> resolveEditor(editable: E): Editor<E>
    @Suppress("UNCHECKED_CAST")
    private class Impl(
        private val expanderFactory: ExpanderFactory,
        private val classLoader: ClassLoader
    ) : EditorFactory {

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
                                resolveConstructor(cls, editorCls) as ((Editable<*>) -> Editor<*>)
                            }
                    factory(editable)
                }.let { editorCls.cast(it) }

        private val editorClasses = mutableMapOf<KClass<Editable<*>>, KClass<Editor<*>>>()

        override fun <E : Editable<*>, Ed : Editor<E>> registerEditorClass(
            editableCls: KClass<E>,
            editorCls: KClass<Ed>
        ) {
            editorClasses[editableCls as KClass<Editable<*>>] = editorCls as KClass<Editor<*>>
        }


        override fun <E : Editable<*>> resolveEditor(editable: E): Editor<E> {
            if (editable is Expandable<*, *>) return expanderFactory.getExpander(editable) as Editor<E>
            val editorCls = getEditorClass(editable::class)
            return getEditor(editorCls, editable)
        }

        private fun <E : Editable<*>, Ed : Editor<E>> getEditorClass(editableCls: KClass<out E>): KClass<Ed> {
            val userSpecified = editorClasses[editableCls as KClass<Editable<*>>]
            return if (userSpecified != null) userSpecified as KClass<Ed>
            else {
                val cls = resolveEditorClass<E, Ed>(editableCls as KClass<E>)
                          ?: throw NoSuchElementException("Could not find editor class for $editableCls")
                registerEditorClass(editableCls, cls as KClass<Editor<Editable<*>>>)
                return cls
            }
        }

        private fun <E : Editable<*>, Ed : Editor<E>> resolveEditorClass(editableCls: KClass<E>): KClass<Ed>? {
            val name = editableCls.simpleName ?: return null
            val pkg = editableCls.java.`package`?.name ?: return null
            val editorClsName = name.removePrefix("Editable") + "Editor"
            val inSamePackage = "$pkg.$editorClsName"
            val inEditorPackage = "$pkg.editor.$editorClsName"
            val siblingEditorPkg = pkg.replaceAfterLast('.', "editor")
            val inSiblingEditorPkg = "$siblingEditorPkg.$editorClsName"
            return tryCreateEditorCls(inSamePackage)
                   ?: tryCreateEditorCls(inEditorPackage)
                   ?: tryCreateEditorCls(inSiblingEditorPkg)
        }

        @Suppress("UNCHECKED_CAST")
        private fun <Ed : Editor<*>> tryCreateEditorCls(name: String): KClass<Ed>? {
            return try {
                val cls = classLoader.loadClass(name)
                val k = cls.kotlin
                k.takeIf { it.isSubclassOf(Editor::class) } as KClass<Ed>?
            } catch (cnf: ClassNotFoundException) {
                null
            }
        }

        companion object {
            private fun <E : Editable<*>, Ed : Editor<E>> resolveConstructor(
                cls: KClass<out E>,
                editorCls: KClass<Ed>
            ): ((E) -> Ed)? {
                val constructor = editorCls.constructors
                                          .find {
                                              it.parameters.count { p -> !p.isOptional } == 1 &&
                                              it.parameters.first().type.isSupertypeOf(cls.starProjectedType)
                                          }
                                  ?: throw NoSuchElementException("No constructor valid found for $editorCls")
                return { editable -> constructor.callBy(mapOf(constructor.parameters.first() to editable)) }
            }
        }

    }

    companion object : Property<EditorFactory, Public, Internal>("editor factory") {
        val logger = Logger.getLogger(EditorFactory::class.qualifiedName)
        fun newInstance(expanderFactory: ExpanderFactory, classLoader: ClassLoader): EditorFactory =
                Impl(expanderFactory, classLoader)

        @CallerSensitive
        fun newInstance(expanderFactory: ExpanderFactory) =
                newInstance(expanderFactory, Reflection.getCallerClass().classLoader)

    }
}

inline fun <reified E : Editable<*>, reified Ed : Editor<E>> EditorFactory.register(noinline factory: (E) -> Ed) {
    register(E::class, Ed::class, factory)
}

inline fun <E : Editable<*>, reified Ed : Editor<E>> EditorFactory.getEditor(editable: E) =
        getEditor(Ed::class, editable)
