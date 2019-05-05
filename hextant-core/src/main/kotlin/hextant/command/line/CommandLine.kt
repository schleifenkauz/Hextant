/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.*
import hextant.base.AbstractController
import hextant.command.*
import hextant.command.line.CommandLine.State.EditingArguments
import hextant.command.line.CommandLine.State.EditingName
import hextant.impl.SelectionDistributor
import reaktive.value.now

class CommandLine(
    private val targets: Collection<Any>,
    private val commands: () -> Set<Command<*, *>>,
    private val context: Context
) : AbstractController<CommandLineView>() {
    private sealed class State {
        class EditingName(var currentName: String) : State()

        class EditingArguments(
            val command: Command<Any, *>,
            val argumentEditors: List<Editor<*>>
        ) : State()
    }

    private var state: State = EditingName("")

    override fun viewAdded(view: CommandLineView) {
        when (val state = state) {
            is EditingName      -> views { editingName(state.currentName) }
            is EditingArguments -> views {
                editingArguments(state.command.name, state.command.parameters, state.argumentEditors)
            }
        }
    }

    fun editName(newName: String) {
        val state = state
        check(state is EditingName) { "Cannot edit name while editing arguments" }
        state.currentName = newName
        views {
            this.displayText(newName)
        }
    }

    fun executeOrExpand() {
        when (val state = state) {
            is EditingName      -> {
                val command = commands().find {
                    it.shortName == state.currentName && targets.all { target -> it.isApplicableOn(target) }
                } ?: return
                @Suppress("UNCHECKED_CAST") //This is save because we checked, that it is applicable on all targets
                command as Command<Any, *>
                if (command.parameters.isEmpty()) {
                    val results = targets.map { receiver -> command.execute(receiver, emptyList()) }
                    val application = CommandApplication(command, emptyList(), results)
                    views { executed(application) }
                    state.currentName = ""
                    views { editingName("") }
                } else {
                    val editors = command.parameters.map { p -> context.createEditor(p.type) }
                    this.state = EditingArguments(command, editors)
                    views { editingArguments(command.name, command.parameters, editors) }
                }
            }
            is EditingArguments -> {
                val arguments = state.argumentEditors.map {
                    it.result.now.orElse { return@executeOrExpand }.force()
                }
                val results = targets.map { receiver -> state.command.execute(receiver, arguments) }
                val application = CommandApplication(state.command, arguments, results)
                views { executed(application) }
                setEditingName("")
            }
        }
    }

    fun resume(application: CommandApplication) {
        val command = application.command
        if (targets.any { !command.isApplicableOn(it) }) return
        val name = command.shortName!!
        if (application.args.isEmpty()) {
            setEditingName(name)
        } else {
            val editors = application.args.map { context.createEditor(it) }
            @Suppress("UNCHECKED_CAST")
            state = EditingArguments(command as Command<Any, *>, editors)
            views { editingArguments(name, command.parameters, editors) }
        }
    }

    fun reset() {
        setEditingName("")
    }

    private fun setEditingName(name: String) {
        state = EditingName(name)
        views { editingName(name) }
    }

    fun availableCommands(): Set<Command<*, *>> = commands()

    companion object {
        fun forSelectedEditors(selection: SelectionDistributor, context: Context): CommandLine {
            val commands = context[Commands]
            val commandsFactory = {
                val possibleCommands = selection.selectedTargets.now.asSequence().map {
                    commands.applicableOn(it)
                }
                if (possibleCommands.none()) emptySet()
                else possibleCommands.reduce { acc, s -> acc.intersect(s) }
            }
            return CommandLine(selection.selectedTargets.now, commandsFactory, context)
        }
    }
}