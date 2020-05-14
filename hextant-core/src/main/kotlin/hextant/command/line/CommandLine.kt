/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.*
import hextant.base.AbstractEditor
import hextant.command.Command
import hextant.serial.makeRoot
import reaktive.Observer
import reaktive.dependencies
import reaktive.value.*
import reaktive.value.binding.binding

/**
 * ### An editor for [Command]s.
 */
class CommandLine(context: Context, private val source: CommandSource) :
    AbstractEditor<CommandApplication, CommandLineView>(context) {
    private var commandName: String = ""
    private var arguments: List<Editor<*>>? = null
    private var expandedCommand: Command<*, *>? = null
    private var history = mutableListOf<HistoryItem>()

    /**
     * Returns `true` only if this [CommandLine] is in the expanded state.
     */
    val expanded get() = expandedCommand != null

    private val _result: ReactiveVariable<CompileResult<CommandApplication>> = reactiveVariable(childErr())
    private var obs: Observer? = null
    override val result: EditorResult<CommandApplication> get() = _result

    /**
     * Change the input command name to the given [name].
     * @throws IllegalStateException if this [CommandLine] is expanded.
     */
    fun setCommandName(name: String) {
        check(!expanded) { "Command already expanded, can't change name to '$name'" }
        commandName = name
        views { displayCommandName(name) }
    }

    /**
     * Returns a collection of all the command applicable on the current targets.
     */
    fun availableCommands(): Collection<Command<*, *>> {
        val available = mutableSetOf<Command<*, *>>()
        val targets = source.selectedTargets()
        if (targets.isNotEmpty()) available.addAll(source.commandsFor(targets.first()))
        for (t in targets.drop(1)) available.retainAll(source.commandsFor(t))
        val focused = source.focusedTarget()
        if (focused != null) {
            source.commandsFor(focused).filterTo(available) { it.commandType == Command.Type.SingleReceiver }
        }
        if (focused is Editor<*> && focused.expander != null) {
            val exp = focused.expander!!
            available.addAll(source.commandsFor(exp))
        }
        return available
    }

    /**
     * Expand this [CommandLine] by searching for an applicable command with the current [commandName] and instantiating
     * the editors needed for the command arguments.
     * @return `true` only if this [CommandLine] was successfully expanded.
     */
    fun expand(): Boolean {
        if (expanded) return false
        val cmd = availableCommands().find { it.shortName == commandName } ?: return false
        return expand(cmd)
    }

    /**
     * Expand this [CommandLine] by instantiating the argument editors for the given [command]
     * @return `true` only if this [CommandLine] was successfully expanded.
     */
    fun expand(command: Command<*, *>): Boolean {
        if (expanded) return false
        val editors = command.parameters.map { p ->
            context.createEditor(p.type).also { it.makeRoot() }
        }
        bindResult(command, editors.map { it.result })
        expanded(command, editors)
        return true
    }

    private fun expanded(
        cmd: Command<*, *>,
        editors: List<Editor<Any>>
    ) {
        arguments = editors
        expandedCommand = cmd
        views { expanded(cmd, editors) }
    }

    private fun bindResult(
        cmd: Command<*, *>,
        arguments: List<EditorResult<Any>>
    ) {
        obs = _result.bind(binding<CompileResult<CommandApplication>>(dependencies(arguments)) {
            val args = mutableListOf<Any>()
            for (result in arguments) {
                val value = result.now.ifErr { return@binding childErr() }
                args.add(value)
            }
            ok(CommandApplication(cmd, args))
        })
    }

    /**
     * Execute the current edited command.
     * If this command line is not expanded an attempt is made to [expand] it.
     * When the arguments are not all ok this method has no effect.
     * After the command is executed the command line is [reset].
     * @return `true` only if the command was successfully executed.
     */
    fun execute(): Boolean {
        if (!expanded && !expandNoArgCommand()) return false
        val application = result.now.ifErr { return false }
        val focused = source.focusedTarget()
        if (focused != null && application.command.commandType == Command.Type.SingleReceiver) {
            val applicable = application.command.isApplicableOn(focused)
            var target = focused
            if (!applicable) {
                if (focused !is Editor<*>) return false
                if (focused.expander == null) return false
                val e = focused.expander!!
                if (!application.command.isApplicableOn(e)) return false
                target = e
            }
            val res = application.execute(target)
            commandExecuted(application.command, application.args, res)
        } else {
            val results = source.selectedTargets().map { application.execute(it) }
            commandExecuted(application.command, application.args, results.singleOrNull() ?: Unit)
        }
        reset()
        return true
    }

    private fun commandExecuted(command: Command<*, *>, arguments: List<Any>, result: Any) {
        history.add(HistoryItem(command, arguments, result))
        views {
            addToHistory(command, arguments, result)
        }
    }

    private fun expandNoArgCommand(): Boolean {
        val cmd = availableCommands().find { it.shortName == commandName && it.parameters.isEmpty() } ?: return false
        _result.set(ok(CommandApplication(cmd, emptyList())))
        expanded(cmd, emptyList())
        return true
    }

    /**
     * Expand to the given [command] and instantiate the editors for the given [arguments].
     */
    fun resume(
        command: Command<*, *>,
        arguments: List<Any>
    ) {
        reset()
        setCommandName(command.shortName!!)
        val editors = arguments.map { context.createEditor(it) }
        bindResult(command, editors.map { it.result })
        expanded(command, editors)
    }

    override fun viewAdded(view: CommandLineView) {
        view.displayCommandName(commandName)
        if (expanded) {
            view.expanded(expandedCommand!!, arguments!!)
        }
        for ((cmd, args, res) in history) view.addToHistory(cmd, args, res)
    }

    /**
     * Reset this command line by:
     * * Deleting the argument editors
     * * Resetting the command name to an empty string
     * * Clearing the [result]
     * @return `true` only if the command line was successfully reset.
     */
    fun reset(): Boolean {
        if (!expanded) return false
        arguments = null
        expandedCommand = null
        commandName = ""
        obs?.kill()
        obs = null
        _result.set(childErr())
        views { reset() }
        return true
    }

    private data class HistoryItem(
        val command: Command<*, *>,
        val arguments: List<Any>,
        val result: Any
    )
}