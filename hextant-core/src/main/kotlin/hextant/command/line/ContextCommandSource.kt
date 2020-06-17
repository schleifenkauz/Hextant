/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.*
import hextant.command.Command
import hextant.command.Command.Type.MultipleReceivers
import hextant.command.Command.Type.SingleReceiver
import hextant.command.Commands
import hextant.command.line.CommandReceiverType.*
import reaktive.collection.binding.all
import reaktive.collection.binding.anyR
import reaktive.list.reactive
import reaktive.set.ReactiveSet
import reaktive.set.binding.mapNotNull
import reaktive.value.*
import reaktive.value.binding.map

/**
 * A [CommandSource] the uses the [SelectionDistributor] and the [Commands] of the given context.
 */
class ContextCommandSource(
    private val distributor: SelectionDistributor,
    private val commands: Commands,
    receiverTypes: Set<CommandReceiverType>
) : CommandSource {
    constructor(
        distributor: SelectionDistributor,
        commands: Commands,
        vararg receiverTypes: CommandReceiverType
    ) : this(distributor, commands, receiverTypes.toSet())

    constructor(context: Context, vararg receiverTypes: CommandReceiverType) : this(
        context[SelectionDistributor],
        context[Commands],
        *receiverTypes
    )

    private val selectedReceivers = receiverTypes.map { t -> selectedReceivers(t) }
    private val focusedReceivers = receiverTypes.map { t -> focusedReceiver(t) }

    private fun selectedReceivers(type: CommandReceiverType): ReactiveSet<Any> = when (type) {
        Views     -> distributor.selectedViews
        Targets   -> distributor.selectedTargets
        Expanders -> distributor.selectedTargets.mapNotNull { t -> (t as? Editor<*>)?.expander }
    }

    private fun focusedReceiver(type: CommandReceiverType): ReactiveValue<Any?> = when (type) {
        Views     -> distributor.focusedView
        Targets   -> distributor.focusedTarget
        Expanders -> distributor.focusedTarget.map { t -> (t as? Editor<*>)?.expander }
    }

    override fun executeCommand(command: Command<*, *>, arguments: List<Any?>): List<Any?> =
        when (command.commandType) {
            SingleReceiver    -> focusedReceivers.asSequence()
                .map { it.now }
                .filterNotNull()
                .filter { r -> command.isApplicableOn(r) }
                .map { r ->
                    @Suppress("UNCHECKED_CAST") //we checked this with the filter
                    command as Command<Any, Any?>
                    command.execute(r, arguments)
                }.toList()
            MultipleReceivers -> selectedReceivers.asSequence()
                .filter { selected -> selected.now.all { command.isApplicableOn(it) } }
                .flatMap { selected -> selected.now.asSequence() }
                .map { r ->
                    @Suppress("UNCHECKED_CAST") //we checked this with the filter
                    command as Command<Any, Any?>
                    command.execute(r, arguments)
                }.toList()
        }

    override fun availableCommands(): Collection<Command<*, *>> = selectedReceivers
        .flatMap { selected ->
            intersect(selected.now.map { r ->
                commands.applicableOn(r).filter { it.commandType == MultipleReceivers }
            })
        }
        .union(focusedReceivers.flatMap { focused ->
            focused.now?.let { f ->
                commands.applicableOn(f).filter { c -> c.commandType == SingleReceiver }
            } ?: emptySet<Command<*, *>>()
        })

    override fun isApplicable(command: Command<*, *>): ReactiveBoolean = when (command.commandType) {
        MultipleReceivers -> selectedReceivers.reactive().anyR { selected ->
            selected.all { r -> command.isApplicableOn(r) }
        }
        SingleReceiver    -> focusedReceivers.reactive().anyR { focused ->
            focused.map { r -> r != null && command.isApplicableOn(r) }
        }
    }

    companion object {
        private fun <E> intersect(sets: Iterable<Collection<E>>): Set<E> {
            if (!sets.any()) return emptySet()
            val intersection = sets.first().toMutableSet()
            for (e in sets.drop(1)) intersection.retainAll(e)
            return intersection
        }
    }
}