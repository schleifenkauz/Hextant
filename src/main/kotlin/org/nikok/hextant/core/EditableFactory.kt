/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package org.nikok.hextant.core

import org.nikok.hextant.Editable
import org.nikok.hextant.core.editable.map
import org.nikok.hextant.core.impl.ClassMap
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.prop.Property
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

interface EditableFactory {
    /**
     * Register the [factory] for the [editedCls]
    */
    fun <T : Any> register(editedCls: KClass<T>, factory: () -> Editable<T>)

    fun <T : Any> register(editedCls: KClass<T>, factory: (T) -> Editable<T>)

    fun <T : Any> getEditable(editedCls: KClass<T>): Editable<T>

    fun <T : Any> getEditable(edited: T): Editable<T>

    fun <T : Any, F : Any> registerConversion(tCls: KClass<T>, fCls: KClass<F>, convert: (T?) -> F?)

    fun <T : Any, F : Any> registerConversionNotNull(tCls: KClass<T>, fCls: KClass<F>, convert: (T) -> F)

    companion object: Property<EditableFactory, Public, Internal>("editable factory") {
        /**
         * @return a new [EditableFactory]
        */
        fun newInstance(): EditableFactory = Impl()
    }

    private class Impl : EditableFactory {
        /*
         * Maps the class of the produced values to a pair of the converted class
         * and a function taking the converted class and producing the target
         * Could be read as Map<KClass<TargetType>, Pair<KClass<SourceType>, (SourceType) -> TargetType>>
        */
        private val conversions = ClassMap.invariant<MutableSet<Pair<KClass<*>, (Any?) -> Any?>>>()

        override fun <T : Any, F : Any> registerConversion(tCls: KClass<T>, fCls: KClass<F>, convert: (T?) -> F?) {
            val conversion = tCls to convert as (Any?) -> (F?)
            val existingConversions = conversions[fCls]
            if (existingConversions != null) existingConversions.add(conversion)
            else conversions[fCls] = mutableSetOf(conversion)
        }

        override fun <T : Any, F : Any> registerConversionNotNull(tCls: KClass<T>, fCls: KClass<F>, convert: (T) -> F) {
            registerConversion(tCls, fCls) { it?.let(convert) }
        }

        private val oneArgFactories = ClassMap.covariant<(Any) -> Editable<Any>>()

        override fun <T : Any> register(editedCls: KClass<T>, factory: (T) -> Editable<T>) {
            oneArgFactories[editedCls] = factory as (Any) -> Editable<Any>
        }

        private fun <T : Any> getEditable(edited: T, forbiddenSources: Set<KClass<*>>): Pair<Editable<T>?, Int> {
            val editedCls = edited::class
            val factory = oneArgFactories[editedCls]
            if (factory != null) return factory(edited) as Editable<T> to 0
            val default = getDefaultEditable(edited)
            if (default != null) return default to 0
            return getConvertedEditable(edited, editedCls, forbiddenSources)
        }

        override fun <T : Any> getEditable(edited: T): Editable<T> {
            val (e, _) = getEditable(edited, forbiddenSources = emptySet())
            if (e != null) return e
            else throw NoSuchElementException("No one-arg factory configured for ${edited.javaClass}")
        }

        private fun <T : Any> getConvertedEditable(
            edited: T, cls: KClass<out T>, forbiddenSources: Set<KClass<*>>
        ): Pair<Editable<T>?, Int> {
            val conversions = conversions[cls] ?: return null to -1
            TODO()

        }

        private val noArgFactories = ClassMap.contravariant<() -> Editable<*>>()

        override fun <T : Any> register(editedCls: KClass<T>, factory: () -> Editable<T>) {
            noArgFactories[editedCls] = factory
        }

        private fun <T : Any> getConvertedEditable(
            cls: KClass<T>, forbiddenSources: Set<KClass<*>>
        ): Pair<Editable<T>?, Int> {
            val conversions = conversions[cls] ?: return null to -1
            val srcAndConverterAndConversionCount = conversions.mapNotNull { (srcCls, c) ->
                if (srcCls in forbiddenSources) null
                else {
                    val (src, conversionCount) = getEditable(srcCls, forbiddenSources + cls)
                    if (src == null) null
                    else Triple(src, c, conversionCount)
                }
            }
            val (src, converter, conversionCount) = srcAndConverterAndConversionCount.minBy { (_, _, conversionCount) -> conversionCount }
                                                    ?: return null to -1
            val converted = src.map(converter) as Editable<T>
            return converted to conversionCount + 1
        }

        private fun <T : Any> getEditable(
            editedCls: KClass<T>, forbiddenSources: Set<KClass<*>>
        ): Pair<Editable<T>?, Int> {
            val factory = noArgFactories[editedCls]
            if (factory != null) return factory() as Editable<T> to 0
            val default = getDefaultEditable(editedCls)
            if (default != null) return default to 0
            return getConvertedEditable(editedCls, forbiddenSources)
        }

        override fun <T : Any> getEditable(editedCls: KClass<T>): Editable<T> {
            val (editable, _) = getEditable(editedCls, forbiddenSources = emptySet())
            if (editable == null) throw NoSuchElementException("Cannot create editable for $editedCls")
            return editable
        }

        @Suppress("FunctionName") private companion object {
            fun <T : Any> `locate editable cls in "editable" package`(
                pkgName: String, name: String
            ): KClass<Editable<T>>? {
                return if (pkgName.endsWith("edited")) {
                    val editedRange = pkgName.length - "edited".length..pkgName.length
                    val editablePkg = pkgName.replaceRange(editedRange, "editable")
                    tryFindCls("$editablePkg.Editable$name")
                } else {
                    tryFindCls("$pkgName.editable.Editable$name")
                }
            }

            fun <T : Any> resolveEditableCls(editedCls: KClass<T>): KClass<Editable<T>>? {
                val java = editedCls.java
                val pkg = java.`package` ?: return null
                val name = editedCls.simpleName ?: return null
                val pkgName = pkg.name
                return `locate editable cls in "editable" package`(pkgName, name)
                       ?: `editable class with "Editable" prefix`(name, pkgName)
            }

            fun <T : Any> `editable class with "Editable" prefix`(
                name: String, pkgName: String?
            ): KClass<Editable<T>>? {
                val editableName = "Editable$name"
                val editableClsName = "$pkgName.$editableName"
                return tryFindCls(editableClsName)
            }

            fun <T : Any> tryFindCls(editableClsName: String): KClass<Editable<T>>? {
                try {
                    val editableCls: KClass<*> = Class.forName(editableClsName).kotlin
                    if (!editableCls.isSubclassOf(Editable::class)) return null
                    return editableCls as KClass<Editable<T>>
                } catch (notFound: ClassNotFoundException) {
                    return null
                }
            }

            fun <T : Any> getDefaultEditable(editedCls: KClass<T>): Editable<T>? {
                val cls = resolveEditableCls(editedCls) ?: return null
                val noArgConstructor =
                        cls.constructors.find { c -> c.parameters.all { p -> p.isOptional } } ?: return null
                return noArgConstructor.call()
            }

            fun <T : Any> getDefaultEditable(edited: T): Editable<T>? {
                val editedCls = edited::class
                val cls = resolveEditableCls(editedCls) ?: return null
                val oneArgConstructor = cls.constructors.find { c ->
                    c.parameters.size == 1 && c.parameters.first().type.classifier == editedCls
                } ?: return null
                return oneArgConstructor.call(edited)
            }

        }
    }
}