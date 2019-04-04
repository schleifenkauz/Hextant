/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.*
import hextant.base.AbstractEditable
import hextant.command.*
import hextant.command.line.CommandLine.State.EditingArgs
import hextant.command.line.CommandLine.State.EditingName
import hextant.core.EditableFactory
import hextant.impl.SelectionDistributor
import hextant.impl.myLogger
import reaktive.event.event
import reaktive.value.*

/**
 * The model of a Command line
 *
 * A command line can basically be in two states
 * 1. The name of the command searched is being edited, we will call this state "editing name"
 * 2. The args of the command searched are being edited, we will call this state "editing args"
 *
 * @constructor
 * @param commandsFactory is used to get the commands being applicable in this context
 * @param targets the targets that will be the receivers for a command when executed by this command line
 * defaults to the editable factory of the [HextantPlatform]
 */
class CommandLine(
    private val commandsFactory: () -> Set<Command<*, *>>,
    private val targets: () -> Set<Any>,
    context: Context
) : AbstractEditable<CommandApplication<*>>() {
    private val editableFactory = context[EditableFactory]

    /*
     * The current state
    */
    private val mutableState = reactiveVariable(EditingName)

    /*
     * Is fired when a command was executed by this command line
    */
    private val execute = event<CommandApplication<Any>>()

    /**
     * An event stream emitting [CommandApplication]s when a command was executed by this command line
     */
    val executed = execute.stream

    /**
     * The current state of this command line
     */
    val state: ReactiveValue<State> get() = mutableState

    /**
     * The state of a command line
     */
    enum class State {
        /**
         * The state of a command line when the user edits the name of the command, see [CommandLine]
         */
        EditingName,
        /**
         * The state of a command line when the user edits the arguments of the command, see [CommandLine]
         */
        EditingArgs
    }

    /*
     * The name of the command being searched
    */
    private val mutableText = reactiveVariable("")

    /**
     * The name of the command being searched
     */
    val text: ReactiveValue<String> get() = mutableText

    private val mutableResult = reactiveVariable<CompileResult<CommandApplication<*>>>(Err("No command"))

    override val result get() = mutableResult

    /**
     * Set the name of the command being searched to [new]
     * @throws IllegalStateException if the arguments are currently being edited
     */
    fun setText(new: String) {
        check(new == text.now || state.now == EditingName) { "Cannot set text while editing args" }
        logger.info("Set text to $new")
        mutableText.set(new)
    }

    /*
     * The currently edited command, null if this command line is in the state "editing name"
    */
    private var editedCommand: Command<Any, *>? = null

    /**
     * @return the currently edited command
     * @throws IllegalStateException if this command line is in the state "editing name"
     */
    fun editedCommand() = editedCommand ?: throw IllegalStateException("No edited command")

    /*
     * The args edited by the user or null if the state is "editing name"
    */
    private var editableArgs: List<Editable<Any>>? = null

    /**
     * @return the currently edited arguments
     * @throws IllegalStateException if the command line is in the state "editing name"
     */
    fun editableArgs() = editableArgs ?: throw IllegalStateException("No edited args")

    /**
     * If the command line is in state "editing name":
     * * It will try to find a command with the current edited [text] as name
     * * If there is no such command, it returns without any effect
     * * If there is such a command that has no parameters, it will just execute this command
     * * If there is such a command which has one or more parameters, it will change the state to "editing args"
     * If the command line is in state "editing args"
     * * It will lookup whether all editable arguments are ok
     * * If so it will execute the command with the edited arguments
     * * Else it will just do nothing
     */
    fun executeOrExpand() {
        logger.info("Executing or expanding")
        val targets = targets()
        logger.fine { "targets = $targets" }
        when (state.now) {
            EditingName -> executeOrExpandEditingName(targets)
            EditingArgs -> tryExecute(targets)
        }
    }

    /**
     * The commands pool in the current context
     */
    fun commands() = commandsFactory()

    private fun tryExecute(targets: Set<Any>) {
        compile {
            val args = editableArgs().map { it.result.now.orTerminate() }
            execute(editedCommand(), targets, args.toTypedArray())
            ok(Unit)
        }
    }

    @Suppress("UNCHECKED_CAST") private fun executeOrExpandEditingName(targets: Set<Any>) {
        logger.fine { "Commandline is editing name" }
        val commands = commands() as Set<Command<Any, *>>
        logger.fine { "Commands = $commands" }
        val c = commands.findCommand(targets) ?: return
        logger.fine { "Found command $c" }
        if (c.parameters.isEmpty()) {
            logger.fine("parameters are empty, just executing")
            val res = execute(c, targets, emptyArray())
            logger.fine { "Result = $res" }
        } else {
            editableArgs = c.parameters.map { p -> editableFactory.getEditable(p.type) }
            logger.fine { "args are now $editableArgs" }
            editedCommand = c
            logger.fine { "editedCommand = $c" }
            mutableState.set(EditingArgs)
            logger.fine { "state = $EditingArgs" }
        }
    }

    private fun execute(c: Command<Any, *>, targets: Set<Any>, arguments: Array<Any>) {
        val applicableTargets = targets.filter { c.isApplicableOn(it) }
        val results = applicableTargets.map { c.execute(it, *arguments) }
        logger.fine { "results = $results" }
        val application = CommandApplication(c, arguments, results)
        execute.fire(application)
        reset()
    }

    /**
     * Reset the command line by
     * * Setting the state to "editing name"
     * * Setting the text to and empty string
     */
    fun reset() {
        logger.info { "resetting" }
        editableArgs = null
        editedCommand = null
        mutableText.set("")
        mutableState.set(EditingName)
    }

    /**
     * Resume to the given [application] by
     * * Looking up if the applied command is applicable in the current context, if not return
     * * Setting the edited name to the name of the applied command
     * * If the arguments are empty setting the state to "editing name"
     * * If the arguments are not empty setting the editable args to the arguments of the [application] and setting the state to "editing args"
     */
    fun resume(application: CommandApplication<Any>) {
        logger.info { "Resuming to $application" }
        val c = application.command
        if (targets().none { c.isApplicableOn(it) }) {
            logger.fine { "Cannot execute command on all targets" }
            return
        }
        val name = c.shortName!!
        val args = application.args
        mutableText.set(name)
        if (args.isEmpty()) {
            mutableState.set(EditingName)
        } else {
            val editable = args.map { editableFactory.getEditable(it) }
            editableArgs = editable
            editedCommand = c
            mutableState.set(EditingArgs)
        }
    }

    private fun Set<Command<Any, *>>.findCommand(targets: Set<Any>): Command<Any, *>? {
        return find { c ->
            c.shortName == text.now && targets.any { t ->
                c.isApplicableOn(t)
            }
        }
    }

    override fun toString(): String = buildString {
        appendln("Command Line")
        append("state = $state")
        append("text = ${text.now}")
        if (editableArgs != null) append("editable arguments = $editableArgs")
        if (editedCommand != null) append("edited command = $editedCommand")
    }

    companion object {
        fun forSelectedEditors(selection: SelectionDistributor, context: Context): CommandLine {
            val commands = context[Commands]
            val targets = {
                selection.selectedEditors.now
            }
            val commandsFactory = {
                targets().asSequence().map {
                    commands.applicableOn(it)
                }.reduce { acc, s -> acc.union(s) }
            }
            return CommandLine(commandsFactory, targets, context)
        }

        val logger by myLogger()
    }
}