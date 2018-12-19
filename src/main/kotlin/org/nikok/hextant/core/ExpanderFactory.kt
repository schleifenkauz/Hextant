/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core

import org.nikok.hextant.*
import org.nikok.hextant.bundle.Property
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.editable.Expandable
import org.nikok.hextant.core.editor.Expander
import org.nikok.hextant.core.impl.DoubleWeakHashMap
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.*

interface ExpanderFactory {
    fun <E : Editable<*>> getExpander(expandable: Expandable<*, E>): Expander<E>

    fun <E : Editable<*>, Ex : Expandable<*, E>> register(
        expandableCls: KClass<Ex>,
        constructor: (Ex) -> Expander<E>
    )

    @Suppress("UNCHECKED_CAST")
    private class Impl(
        private val classLoader: ClassLoader,
        private val context: Context
    ) : ExpanderFactory {
        private val cache = DoubleWeakHashMap<Expandable<*, *>, Expander<*>>()

        private val factories = mutableMapOf<KClass<Expandable<*, *>>, (Expandable<*, *>) -> Expander<*>>()

        override fun <E : Editable<*>, Ex : Expandable<*, E>> register(
            expandableCls: KClass<Ex>,
            constructor: (Ex) -> Expander<E>
        ) {
            factories[expandableCls as KClass<Expandable<*, *>>] = constructor as (Expandable<*, *>) -> Expander<*>
        }

        override fun <E : Editable<*>> getExpander(expandable: Expandable<*, E>): Expander<E> {
            return cache.getOrPut(expandable) { createNewExpander(expandable) } as Expander<E>
        }

        private fun <E : Editable<*>> createNewExpander(expandable: Expandable<*, E>): Expander<*> {
            val expandableCls = expandable::class
            val factory = factories[expandableCls as KClass<Expandable<*, *>>] as ((Expandable<*, E>) -> Expander<E>)?
            if (factory != null) return factory(expandable)
            val default = getDefaultFactory(expandable, expandableCls as KClass<out Expandable<*, E>>)
            return default(expandable)
        }

        private fun <E : Editable<*>> getDefaultFactory(
            expandable: Expandable<*, E>,
            expandableCls: KClass<out Expandable<*, E>>
        ): (Expandable<*, *>) -> Expander<E> {
            val cls =
                    expanderCls(expandable::class)
                    ?: throw NoSuchElementException("No expander class found for $expandableCls")
            val constructor = getExpanderConstructor(cls, expandableCls)
            register(expandableCls, constructor)
            return constructor
        }

        private fun <E : Editable<*>> getExpanderConstructor(
            expanderCls: KClass<Expander<E>>,
            expandableCls: KClass<out Expandable<*, E>>
        ): (Expandable<*, *>) -> Expander<E> {
            lateinit var platformParameter: KParameter
                lateinit var expandableParameter: KParameter
                val constructor = expanderCls.constructors.find { constructor ->
                    val parameters = constructor.parameters
                    platformParameter = parameters.find {
                        it.type == HextantPlatform::class.starProjectedType
                    } ?: return@find false
                    expandableParameter = parameters.find {
                        it.type.isSupertypeOf(expandableCls.starProjectedType)
                    } ?: return@find false
                    val otherParameters = parameters - setOf(platformParameter, expandableParameter)
                    otherParameters.count { !it.isOptional } == 0
                } ?: throw NoSuchElementException("Could not find constructor for $expanderCls")
                return { expandable ->
                    constructor.callBy(
                        mapOf(
                            expandableParameter to expandable,
                            platformParameter to context
                        )
                    )
            }
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
        fun newInstance(classLoader: ClassLoader, context: Context): ExpanderFactory =
            Impl(classLoader, context)
    }
}