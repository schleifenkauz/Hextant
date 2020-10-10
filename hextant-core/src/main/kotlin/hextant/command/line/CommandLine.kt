/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import bundles.SimpleProperty
import hextant.command.Command
import hextant.context.*
import hextant.core.Editor
import hextant.core.editor.*
import hextant.serial.makeRoot
import reaktive.Observer
import reaktive.dependencies
import reaktive.event.EventStream
import reaktive.event.event
import reaktive.value.*
import reaktive.value.binding.binding
import validated.*
import validated.reaktive.ReactiveValidated

/**
 * An editor for [Command]s.
 * @property source the [CommandSource] that is used to resolve available commands
 */
class CommandLine(context: Context, val source: CommandSource) :
    AbstractEditor<CommandApplication, CommandLineView>(context) {
    private var commandName: String = ""
    private var arguments: List<Editor<Any>>? = null
    private val _expandedCommand: ReactiveVariable<Command<*, *>?> = reactiveVariable(null)
    private var history = mutableListOf<HistoryItem>()
    private val expanded = reactiveVariable(false)
    private val execute = event<HistoryItem>()

    /**
     * This [EventStream] emits [HistoryItem]s whenever a command has been executed by this command line.
     */
    val executedCommand: EventStream<HistoryItem> get() = execute.stream

    /**
     * Holds `true` only if this [CommandLine] is in the expanded state.
     */
    val isExpanded get() = expanded

    /**
     * Holds the current expanded command or `null` if the command line is not expanded.
     */
    val expandedCommand get() = _expandedCommand

    private val _result: ReactiveVariable<Validated<CommandApplication>> = reactiveVariable(invalidComponent())
    private var obs: Observer? = null
    override val result: ReactiveValidated<CommandApplication> get() = _result

    /**
     * Change the input command name to the given [name].
     * @throws IllegalStateException if this [CommandLine] is expanded.
     */
    fun setCommandName(name: String) {
        check(!isExpanded.now) { "Command already expanded, can't change name to '$name'" }
        commandName = name
        views { displayCommandName(name) }
    }

    /**
     * Returns a collection of all the command applicable on the current targets.
     */
    fun availableCommands(): Collection<Command<*, *>> = source.availableCommands()

    /**
     * Expand this [CommandLine] by searching for an applicable command with the current [commandName] and instantiating
     * the editors needed for the command arguments.
     * @return `true` only if this [CommandLine] was successfully expanded.
     */
    fun expand(): Boolean {
        if (isExpanded.now) return false
        val cmd = availableCommands().find { it.shortName == commandName } ?: return false
        return expand(cmd)
    }

    /**
     * Expand this [CommandLine] by instantiating the argument editors for the given [command]
     * @return `true` only if this [CommandLine] was successfully expanded.
     */
    fun expand(command: Command<*, *>): Boolean {
        if (isExpanded.now) return false
        val editors = command.parameters.map { p ->
            @Suppress("UNCHECKED_CAST") val v =
                if (p.editWith != null) p.editWith.createEditor(context)
                else context.createEditor(p.type)
            v.makeRoot()
            v
        }
        bindResult(command, editors.map { it.result })
        expanded(command, editors)
        return true
    }

    private fun expanded(cmd: Command<*, *>, editors: List<Editor<Any>>) {
        arguments = editors
        _expandedCommand.now = cmd
        isExpanded.now = true
        views { expanded(cmd, editors) }
    }

    private fun bindResult(
        cmd: Command<*, *>,
        arguments: List<ReactiveValidated<Any>>
    ) {
        obs = _result.bind(binding<Validated<CommandApplication>>(dependencies(arguments)) {
            val args = mutableListOf<Any>()
            for (result in arguments) {
                val value = result.now.ifInvalid { return@binding invalidComponent() }
                args.add(value)
            }
            valid(CommandApplication(cmd, args))
        })
    }

    /**
     * Execute the current edited command.
     * If this command line is not expanded an attempt is made to [expand] it.
     * When the arguments are not all ok this method has no effect.
     * After the command is executed the command line is [reset].
     * @return the result or `null` if the command was not successfully executed.
     */
    fun execute(): Any? {
        if (!isExpanded.now && !expandNoArgCommand()) return null
        val (command, args) = result.now.ifInvalid { return null }
        val onError = listOf("Error executing $command")
        val results = context.executeSafely("Executing $command", onError) {
            val result = source.executeCommand(command, args)
            context[Properties.logger].fine("Executed $command")
            result
        }
        val result = when (results.size) {
            0 -> return null
            1 -> results[0]
            else -> Unit
        }
        commandExecuted(command, arguments ?: emptyList(), result)
        reset()
        return result
    }

    private fun commandExecuted(command: Command<*, *>, arguments: List<Editor<Any>>, result: Any?) {
        val item = HistoryItem(command, arguments.map { it.result.now.force() to it.snapshot() }, result)
        history.add(item)
        execute.fire(item)
        views {
            addToHistory(item)
        }
    }

    private fun expandNoArgCommand(): Boolean {
        val cmd = availableCommands().find { it.shortName == commandName && it.parameters.isEmpty() } ?: return false
        _result.set(valid(CommandApplication(cmd, emptyList())))
        expanded(cmd, emptyList())
        return true
    }

    /**
     * Expand to the given [command] and instantiate the editors for the given [arguments].
     */
    fun resume(command: Command<*, *>, arguments: List<EditorSnapshot<out Editor<Any>>>) {
        reset()
        setCommandName(command.shortName!!)
        val editors = arguments.map {
            context.executeSafely("resuming", null) {
                context.withoutUndo { it.reconstruct(context) }
            } ?: return
        }
        editors.forEach { it.makeRoot() }
        bindResult(command, editors.map { it.result })
        expanded(command, editors)
    }

    override fun viewAdded(view: CommandLineView) {
        view.displayCommandName(commandName)
        if (isExpanded.now) {
            view.expanded(expandedCommand.now!!, arguments!!)
        }
        for (item in history) view.addToHistory(item)
    }

    /**
     * Reset this command line by:
     * * Deleting the argument editors
     * * Resetting the command name to an empty string
     * * Clearing the [result]
     * @return `true` only if the command line was successfully reset.
     */
    fun reset(): Boolean {
        if (!isExpanded.now) return false
        arguments = null
        _expandedCommand.now = null
        isExpanded.now = false
        commandName = ""
        obs?.kill()
        obs = null
        _result.set(invalidComponent())
        views { reset() }
        return true
    }

    /**
     * Used to store the history of a command line.
     */
    data class HistoryItem(
        /**
         * The executed [Command]
         */
        val command: Command<*, *>,
        /**
         * The supplied arguments
         */
        val arguments: List<Pair<Any?, EditorSnapshot<out Editor<Any>>>>,
        /**
         * The result of the application
         */
        val result: Any?
    )

    companion object : SimpleProperty<CommandLine>("command-line")
}