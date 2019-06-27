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

/**
 * Model for a command line.
 * The command line has two different states:
 * 1. The user is currently editing the name of the command to be executed. We will call this state the **search state**
 * 2. The user is currently editing the arguments for the command. We will call this state the **edit state**
 */
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

    /**
     * Change the name of the command that should be executed.
     * @throws IllegalStateException if command line is not in the search state
     */
    fun editName(newName: String) {
        val state = state
        check(state is EditingName) { "Cannot edit name while editing arguments" }
        state.currentName = newName
        views {
            this.displayText(newName)
        }
    }

    /**
     * * If the command line is in the search state, the command with the current command name is looked up.
     *      - If there is no command with the current command name, then the function returns without any effect.
     *      - If the command with the current name has no arguments, it is executed immediately
     *      - Otherwise the command line goes into edit state
     * * If the command line is in the edit state
     *      - The results of the argument editors are collected
     *      - If all results are ok then the command is executed with the collected arguments
     *      - Otherwise nothing happens
     */
    fun executeOrExpand() {
        when (val state = state) {
            is EditingName      -> expand(state)
            is EditingArguments -> execute(state)
        }
    }

    private fun execute(state: EditingArguments) {
        val arguments = state.argumentEditors.map {
            it.result.now.orElse { return@execute }.force()
        }
        val results = targets.map { receiver -> state.command.execute(receiver, arguments) }
        val application = CommandApplication(state.command, arguments, results)
        views { executed(application) }
        setEditingName("")
    }

    private fun expand(state: EditingName) {
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

    /**
     * Resume to the given command application by going into edit state and
     * setting the command name and the arguments to that of the provided command application
     */
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

    /**
     * Reset the command line by going into search state and setting the command name to the empty string.
     * This operation is legal in both states.
     */
    fun reset() {
        setEditingName("")
    }

    private fun setEditingName(name: String) {
        state = EditingName(name)
        views { editingName(name) }
    }

    /**
     * Return all commands available in the current context
     */
    fun availableCommands(): Set<Command<*, *>> = commands()

    companion object {
        /**
         * Return a [CommandLine] with commands available on the editors selected in the given [SelectionDistributor]
         */
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