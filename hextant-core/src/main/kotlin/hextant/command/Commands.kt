/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.command

import bundles.Property
import bundles.property
import hextant.command.meta.collectProvidedCommands
import hextant.context.Internal
import kollektion.ClassDAG
import kollektion.MultiMap
import reaktive.value.now
import kotlin.reflect.KClass

/**
 * Used to register commands for specific classes
 */
class Commands private constructor() {
    private val commands = MultiMap<KClass<*>, Command<*, *>>()
    private val delegations = MultiMap<KClass<*>, (Any) -> Any?>()
    private val all = mutableSetOf<Command<*, *>>()
    private val dag = ClassDAG()

    private fun KClass<*>.reallyAllSuperclasses() = sequence<KClass<*>> {
        val superclass: Class<*>? = java.superclass
        if (superclass != null) yield(superclass.kotlin)
        for (i in java.interfaces) yield(i.kotlin)
    }

    private fun visitClass(cls: KClass<*>) {
        if (dag.insert(cls)) {
            val provided = cls.collectProvidedCommands()
            commands[cls].addAll(provided)
            for (c in cls.reallyAllSuperclasses()) {
                visitClass(c)
                commands[cls].addAll(commands[c])
            }
        }
    }

    /**
     * Register the given [command] for all instances of the specified class.
     */
    fun <R : Any> register(cls: KClass<R>, command: Command<R, *>) {
        all.add(command)
        visitClass(cls)
        dag.subclassesOf(cls).forEach { c -> commands[c].add(command) }
    }

    /**
     * Register the given [delegation] such that all the commands applicable on object of type [F]
     * can automatically be applied on objects of type [D] by delegating the execution to the [F]-object
     * the can be reached from [D] trough the given [delegation] function.
     */
    fun <D : Any, F : Any> registerDelegation(cls: KClass<D>, delegation: (D) -> F?) {
        delegations[cls].add(delegation as (Any) -> Any?)
    }

    /**
     * Unregisters the given [delegation].
     * @see registerDelegation
     */
    fun <D : Any> unregisterDelegation(cls: KClass<D>, delegation: (D) -> Any?) {
        delegations[cls].remove(delegation)
    }

    /**
     * Unregisters the given [command].
     * @throws IllegalStateException if the command has not been registered before.
     */
    fun <R : Any> unregister(command: Command<R, *>) {
        check(all.remove(command)) { "Cannot unregister command $command because it was not registered before" }
        for (cls in dag.subclassesOf(command.receiverCls)) {
            commands[cls].remove(command)
        }
    }

    /**
     * Return a collection of all available commands that are applicable on the given [receiver].
     */
    fun <R : Any> applicableOn(receiver: R): Collection<Command<R, *>> {
        val cls = receiver::class
        visitClass(cls)
        val delegated = dag.superclassesOf(cls).asSequence()
            .flatMap { c -> delegations[c].map { c to it } }
            .flatMap { (c, delegation) ->
                val del = delegation(receiver) ?: return@flatMap emptyList()
                applicableOn(del).map {
                    DelegatedCommand(it, delegation, c) as Command<R, *>
                }
            }
        return forClass(cls).filter { it.isApplicableOn(receiver) } + delegated
    }

    /**
     * Return a collection of all available commands that are applicable on instances of the given class.
     */
    private fun <R : Any> forClass(cls: KClass<out R>): Collection<Command<R, *>> {
        return commands[cls].filter { it.isEnabled.now } as Collection<Command<R, *>>
    }

    /**
     * Return a collection of **all** registered commands.
     */
    fun all(): Collection<Command<*, *>> = all

    companion object : Property<Commands, Internal> by property("commands") {
        /**
         * Return a new [Commands] object
         */
        fun newInstance(): Commands = Commands()
    }
}