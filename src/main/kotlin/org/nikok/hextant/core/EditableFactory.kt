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
import sun.reflect.CallerSensitive
import sun.reflect.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.full.*

/**
 * Used to get editables for edited objects
 *
 * Uses the following naming conventions to search for the classes to instantiate.
 * * If the package of the edited class ends with ".edited" this postfix is replaced with ".editable"
 * and the editable class is searched in a class just prepending "Editable" is searched for in the new package.
 * * If the package doesn't end with ".edited" the same class is searched for in a child package with name "editable".
 * * If no class is found a class with the "Editable"-prefix is searched in the same package.
*/
interface EditableFactory {
    /**
     * Register the [factory] for the [editedCls],
     * such that for any call of `getEditable(editedCls)` this [EditableFactory] uses the specified [factory],
     * where `editedCls` denotes the specified [editedCls] or any superclass-objects,
     * unless another factory has been registered.
    */
    fun <T : Any> register(editedCls: KClass<T>, factory: () -> Editable<T>)

    /**
     * Register the [factory] for the [editedCls],
     * such that for any call of getEditable(edited) this [EditableFactory] uses the specified [factory],
     * where `edited` denotes an instance of exactly [editedCls] not a sub- or superclass instance,
     * unless another factory has been registered.
     */
    fun <T : Any> register(editedCls: KClass<T>, factory: (T) -> Editable<T>)

    /**
     * Tries to find a factory registered with [register] or applies the naming conventions for [Editable]s,
     * which are explained in [EditableFactory] to locate the editable class for [editedCls].
     * Then it tries to find a public constructor without any non-optional parameters and invokes it.
    */
    fun <T : Any> getEditable(editedCls: KClass<T>): Editable<T>

    /**
     * Tries to find a factory registered with [register]
     * or applies the naming conventions for [Editable]s which are explained in [EditableFactory] to locate
     * the editable class for the class of [edited].
     * Then it tries to find a public constructor, whose first parameter is not optional
     * and assignable from [edited] and whose other parameters are optional and invokes it.
     */
    fun <T : Any> getEditable(edited: T): Editable<T>

    /**
     * The editable factory property
    */
    companion object: Property<EditableFactory, Public, Internal>("editable factory") {
        /**
         * @return a new [EditableFactory] using the specified [clsLoader]
        */
        fun newInstance(clsLoader: ClassLoader): EditableFactory = Impl(clsLoader)

        /**
         * @return a new [EditableFactory] using the class loader of the calling class
        */
        @CallerSensitive
        fun newInstance() = newInstance(Reflection.getCallerClass().classLoader)
    }

    private class Impl(private val clsLoader: ClassLoader) : EditableFactory {
        private val oneArgFactories = ClassMap.covariant<(Any) -> Editable<Any>>()

        override fun <T : Any> register(editedCls: KClass<T>, factory: (T) -> Editable<T>) {
            oneArgFactories[editedCls] = factory as (Any) -> Editable<Any>
        }

        override fun <T : Any> getEditable(edited: T): Editable<T> {
            val editedCls = edited::class
            val factory = oneArgFactories[editedCls]
            if (factory != null) return factory(edited) as Editable<T>
            val default = getOneArgConstructor(edited::class)
            if (default != null) return default(edited)
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
            if (default != null) return default()
            throw NoSuchElementException("No no-arg factory found for $editedCls")
        }

        private fun <T : Any> `locate editable cls in "editable" package`(
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

        private fun <T : Any> resolveEditableCls(editedCls: KClass<T>): KClass<Editable<T>>? {
            val java = editedCls.java
            val pkg = java.`package` ?: return null
            val name = editedCls.simpleName ?: return null
            val pkgName = pkg.name
            return `locate editable cls in "editable" package`(pkgName, name)
                   ?: `editable class with "Editable" prefix`(name, pkgName)
        }

        private fun <T : Any> `editable class with "Editable" prefix`(
            name: String, pkgName: String?
        ): KClass<Editable<T>>? {
            val editableName = "Editable$name"
            val editableClsName = "$pkgName.$editableName"
            return tryFindCls(editableClsName)
        }

        private fun <T : Any> tryFindCls(editableClsName: String): KClass<Editable<T>>? {
            try {
                val editableCls: KClass<*> = clsLoader.loadClass(editableClsName).kotlin
                if (!editableCls.isSubclassOf(Editable::class)) return null
                return editableCls as KClass<Editable<T>>
            } catch (notFound: ClassNotFoundException) {
                return null
            }
        }

        private fun <T : Any> getDefaultEditable(editedCls: KClass<T>): (() -> Editable<T>)? {
            val cls = resolveEditableCls(editedCls) ?: return null
            val noArgConstructor =
                    cls.constructors.find { c -> c.parameters.all { p -> p.isOptional } } ?: return null
            return { noArgConstructor.call() }
        }

        private fun <T : Any> getOneArgConstructor(editedCls: KClass<out T>): ((T) -> Editable<T>)? {
            val cls = resolveEditableCls(editedCls) ?: return null
            val oneArgConstructor = cls.constructors.find { c ->
                c.parameters.count { !it.isOptional } == 1 &&
                !c.parameters.first().isOptional &&
                c.parameters.first().type.isSupertypeOf(editedCls.starProjectedType) &&
                c.visibility == PUBLIC
            } ?: return null
            return { edited -> oneArgConstructor.call(edited) }
        }
    }
}