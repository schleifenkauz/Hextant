/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package org.nikok.hextant.core

import org.nikok.hextant.Editable
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.impl.ClassMap
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

    companion object: Property<EditableFactory, Public, Internal>("editable factory") {
        /**
         * @return a new [EditableFactory]
        */
        fun newInstance(): EditableFactory = Impl()
    }

    private class Impl : EditableFactory {
        private val oneArgFactories = ClassMap.covariant<(Any) -> Editable<Any>>()

        override fun <T : Any> register(editedCls: KClass<T>, factory: (T) -> Editable<T>) {
            oneArgFactories[editedCls] = factory as (Any) -> Editable<Any>
        }

        override fun <T : Any> getEditable(edited: T): Editable<T> {
            val editedCls = edited::class
            val factory = oneArgFactories[editedCls]
            if (factory != null) return factory(edited) as Editable<T>
            val default = getDefaultEditable(edited)
            if (default != null) return default
            else throw NoSuchElementException("No one-arg factory found for ${edited.javaClass}")
        }

        private val noArgFactories = ClassMap.contravariant<() -> Editable<*>>()

        override fun <T : Any> register(editedCls: KClass<T>, factory: () -> Editable<T>) {
            noArgFactories[editedCls] = factory
        }

        override fun <T : Any> getEditable(
            editedCls: KClass<T>
        ): Editable<T> {
            val factory = noArgFactories[editedCls]
            if (factory != null) return factory() as Editable<T>
            val default = getDefaultEditable(editedCls)
            if (default != null) return default
            throw NoSuchElementException("No no-arg factory found for $editedCls")
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