/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.*
import hextant.base.AbstractEditor
import hextant.command.Command
import reaktive.Observer
import reaktive.dependencies
import reaktive.value.*
import reaktive.value.binding.binding

/**
 * ### An editor for [Command]s.
 *
 *
 */
class CommandLine(context: Context, private val source: CommandSource) :
    AbstractEditor<CommandApplication, CommandLineView>(context) {
    private var commandName: String = ""
    private var arguments: List<Editor<*>>? = null
    private var expandedCommand: Command<*, *>? = null
    private var history = mutableListOf<HistoryItem>()

    val expanded get() = expandedCommand != null

    private val _result: ReactiveVariable<CompileResult<CommandApplication>> = reactiveVariable(childErr())
    private var obs: Observer? = null
    override val result: EditorResult<CommandApplication> get() = _result

    fun setCommandName(name: String) {
        check(!expanded) { "Command already expanded, can't change name to '$name'" }
        commandName = name
        views { displayCommandName(name) }
    }

    fun availableCommands(): Collection<Command<*, *>> {
        val available = mutableSetOf<Command<*, *>>()
        val targets = source.selectedTargets()
        if (targets.isNotEmpty()) available.addAll(source.commandsFor(targets.first()))
        for (t in targets.drop(1)) available.retainAll(source.commandsFor(t))
        val focused = source.focusedTarget()
        if (focused != null) {
            source.commandsFor(focused).filterTo(available) { it.commandType == Command.Type.SingleReceiver }
        }
        return available
    }

    fun expand(): Boolean {
        if (expanded) return false
        val cmd = availableCommands().find { it.shortName == commandName } ?: return false
        return expand(cmd)
    }

    fun expand(cmd: Command<*, *>): Boolean {
        if (expanded) return false
        val editors = cmd.parameters.map { p -> context.createEditor(p.type) }
        bindResult(cmd, editors.map { it.result })
        expanded(cmd, editors)
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

    fun execute(): Boolean {
        if (!expanded && !expandNoArgCommand()) return false
        val application = result.now.ifErr { return false }
        val focused = source.focusedTarget()
        if (focused != null && application.command.commandType == Command.Type.SingleReceiver) {
            val res = application.execute(focused)
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

    fun resume(
        command: Command<*, *>,
        args: List<Any>
    ) {
        reset()
        setCommandName(command.shortName!!)
        val editors = args.map { context.createEditor(it) }
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