/**
 *@author Nikolaus Knop
 */

package hextant.command

import bundles.Property
import hextant.command.meta.collectProvidedCommands
import hextant.core.Internal
import kollektion.ClassDAG
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

/**
 * Used to register commands for specific classes
 */
class Commands private constructor() {
    private val commands = mutableMapOf<KClass<*>, MutableSet<Command<*, *>>>()
    private val dag = ClassDAG()

    private fun commandsOf(cls: KClass<*>) = commands.getOrPut(cls) { mutableSetOf() }

    private fun visitClass(cls: KClass<*>) {
        if (dag.insert(cls)) {
            val provided = cls.collectProvidedCommands()
            commandsOf(cls).addAll(provided)
            for (c in cls.superclasses) {
                visitClass(c)
                commandsOf(cls).addAll(commandsOf(c))
            }
        }
    }

    /**
     * Register the given [command] for all instances of the specified class.
     */
    fun <R : Any> register(cls: KClass<R>, command: Command<R, *>) {
        visitClass(cls)
        dag.subclassesOf(cls).forEach { c -> commandsOf(c).add(command) }
    }

    /**
     * Return a collection of all available commands that are applicable on the given [receiver].
     */
    fun <R : Any> applicableOn(receiver: R): Collection<Command<R, *>> {
        val cls = receiver::class
        return forClass(cls).filter { it.isApplicableOn(receiver) }
    }

    /**
     * Return a collection of all available commands that are applicable on instances of the given class.
     */
    fun <R : Any> forClass(cls: KClass<out R>): Collection<Command<R, *>> {
        visitClass(cls)
        @Suppress("UNCHECKED_CAST")
        return commandsOf(cls) as Collection<Command<R, *>>
    }

    /**
     * Return a collection of **all** registered commands.
     */
    fun all(): Collection<Command<*, *>> = commands.flatMap { it.value }

    companion object : Property<Commands, Any, Internal>("commands") {
        /**
         * Return a new [Commands] object
         */
        fun newInstance(): Commands = Commands()
    }
}