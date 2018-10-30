/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core

import org.nikok.hextant.Editable
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.editable.Expandable
import org.nikok.hextant.core.editor.Expander
import org.nikok.hextant.prop.Property
import sun.reflect.CallerSensitive
import sun.reflect.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

interface ExpanderFactory {
    fun <E : Editable<*>> getExpander(expandable: Expandable<*, E>): Expander<E>

    @Suppress("UNCHECKED_CAST")
    private class Impl(private val classLoader: ClassLoader) : ExpanderFactory {
        private val cache = mutableMapOf<Expandable<*, *>, Expander<*>>()

        override fun <E : Editable<*>> getExpander(expandable: Expandable<*, E>): Expander<E> {
            return cache.getOrPut(expandable) { createNewExpander(expandable) } as Expander<E>
        }

        private fun <E : Editable<*>> createNewExpander(expandable: Expandable<*, E>): Expander<E> {
            val expandableCls = expandable::class
            val cls =
                    expanderCls(expandable::class)
                    ?: throw NoSuchElementException("No expander class found for $expandableCls")
            return createExpander(cls, expandable, expandableCls)
        }

        private fun <E : Editable<*>> createExpander(
            expanderCls: KClass<Expander<E>>,
            expandable: Expandable<*, E>,
            expandableCls: KClass<out Expandable<*, E>>
        ): Expander<E> {
            val constructor = expanderCls.constructors.find {
                it.parameters.size == 1 &&
                it.parameters[0].type.classifier == expandableCls
            } ?: throw NoSuchElementException("Could not find constructor for $expanderCls")
            return constructor.call(expandable)
        }

        private fun <E : Editable<*>> expanderCls(cls: KClass<out Expandable<*, E>>): KClass<Expander<E>>? {
            val name = cls.simpleName ?: return null
            val pkg = cls.java.`package`?.name ?: return null
            val expanderClsName = name.removePrefix("Expandable") + "Expander"
            val inSamePackage = "$pkg.$expanderClsName"
            val inEditorPackage = "$pkg.editor.$expanderClsName"
            val siblingEditorPkg = pkg.replaceAfterLast('.', "editor")
            val inSiblingEditorPkg = "$siblingEditorPkg.$expanderClsName"
            return tryCreateExpanderCls(inSamePackage) ?: tryCreateExpanderCls(inEditorPackage) ?: tryCreateExpanderCls(
                inSiblingEditorPkg
            )
        }

        @Suppress("UNCHECKED_CAST")
        private fun <E : Editable<*>> tryCreateExpanderCls(name: String): KClass<Expander<E>>? {
            return try {
                val cls = classLoader.loadClass(name)
                val k = cls.kotlin
                k.takeIf { it.isSubclassOf(Expander::class) } as KClass<Expander<E>>?
            } catch (cnf: ClassNotFoundException) {
                null
            }
        }
    }

    companion object : Property<ExpanderFactory, Public, Internal>("expander factory") {
        fun newInstance(classLoader: ClassLoader): ExpanderFactory = Impl(classLoader)

        @CallerSensitive
        fun newInstance(): ExpanderFactory = newInstance(Reflection.getCallerClass().classLoader)
    }
}