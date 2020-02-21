/**
 *@author Nikolaus Knop
 */

package hextant.command

import hextant.bundle.Internal

import hextant.bundle.Property
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
            CommandRegistrar(cls, parents)
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

    companion object : Property<Commands, Any, Internal>("commands") {
        /**
         * Return a new [Commands] object
         */
        fun newInstance(): Commands = Commands()
    }
}