/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.command

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.bundle.Property
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
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
        fun newInstance(platform: HextantPlatform): Commands = Commands()
    }
}