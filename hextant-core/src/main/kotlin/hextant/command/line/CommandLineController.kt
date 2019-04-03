/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.Context
import hextant.base.AbstractEditor
import hextant.command.Command
import hextant.command.line.CommandLine.State
import hextant.command.line.CommandLine.State.EditingArgs
import hextant.completion.Completer
import hextant.core.editor.Expander
import hextant.createView
import reaktive.value.now
import reaktive.value.observe

/**
 * The controller for a [CommandLine]
 * @constructor
 * @param commandLine the [CommandLine] controlled by this controller

 */
class CommandLineController(
    val commandLine: CommandLine,
    private val context: Context
) : AbstractEditor<CommandLine, CommandLineView>(commandLine, context) {
    private val completer: Completer<Command<*, *>> = CommandCompleter { commandLine.commands() }

    private val stateObserver = commandLine.state.observe { _, newState ->
        views {
            handleState(newState, this)
        }
    }

    private val textObserver = commandLine.text.observe { _, newText ->
        views {
            setText(newText)
            if (newText.isNotEmpty()) {
                showCompletions(newText, this)
            }
        }
    }

    private fun showCompletions(newText: String, view: CommandLineView) {
        val completions = completer.completions(newText)
        view.showCompletions(completions)
    }

    /**
     * Show the completions possible in the current context on the given [view]
     */
    fun showCompletions(view: CommandLineView) {
        showCompletions(commandLine.text.now, view)
    }

    private val executeSubscription = commandLine.executed.subscribe { _, appl ->
        views { executed(appl) }
    }

    fun dispose() {
        stateObserver.kill()
        textObserver.kill()
        executeSubscription.cancel()
    }

    override fun viewAdded(view: CommandLineView) {
        handleState(commandLine.state.now, view)
    }

    override val expander: Expander<*, *>?
        get() = null

    private fun handleState(newState: State, view: CommandLineView) {
        if (newState == EditingArgs) {
            val command = commandLine.editedCommand()
            val views = commandLine.editableArgs().map { editable -> context.createView(editable) }
            view.editingArgs(command.shortName!!, command.parameters, views)
        } else {
            view.editingName(commandLine.text.now)
        }
    }
}