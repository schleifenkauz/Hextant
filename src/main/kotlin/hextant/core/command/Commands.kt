/**
 *@author Nikolaus Knop
 */

package hextant.core.command

import hextant.bundle.Property
import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

/**
 * An aggregate of [CommandRegistrar]s
*/
class Commands private constructor() {
    private val commandRegistrars = mutableMapOf<KClass<*>, CommandRegistrar<*>>()

    @Suppress("UNCHECKED_CAST")
    private fun <R : Any> forClass(cls: KClass<out R>): CommandRegistrar<R> {
        return commandRegistrars.getOrPut(cls) {
            val parents = cls.superclasses.map { superCls -> forClass(superCls) }
            CommandRegistrar(parents, cls)
        } as CommandRegistrar<R>
    }

    /**
     * @return the [CommandRegistrar] for receivers of type [R]
    */
    fun <R : Any> of(cls: KClass<out R>): CommandRegistrar<R> = forClass(cls)

    /**
     * @return the [CommandRegistrar] for receivers of type [R]
     */
    inline fun <reified R : Any> of() = of(R::class)

    /**
     * @return the [CommandRegistrar] for receivers of type [R]
     */
    operator fun <R : Any> get(cls: KClass<R>) = of(cls)

    companion object: Property<Commands, Public, Internal>("commands") {
        fun newInstance(): Commands = Commands()
    }
}