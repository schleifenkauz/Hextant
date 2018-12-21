/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.Context
import hextant.base.AbstractEditor
import hextant.command.line.CommandLine.State
import hextant.command.line.CommandLine.State.EditingArgs
import hextant.createView
import org.nikok.reaktive.event.subscribe
import org.nikok.reaktive.value.now
import org.nikok.reaktive.value.observe

/**
 * The controller for a [CommandLine]
 * @constructor
 * @param commandLine the [CommandLine] controlled by this controller

 */
class CommandLineController(
    val commandLine: CommandLine,
    private val context: Context
) : AbstractEditor<CommandLine, CommandLineView>(commandLine, context) {
    private val completer = CommandCompleter

    private val stateObserver = commandLine.state.observe("Observe state of $commandLine") { newState ->
        views {
            handleState(newState, this)
        }
    }

    private val textObserver = commandLine.text.observe("Observe text of $commandLine") { newText ->
        views {
            setText(newText)
            if (newText.isNotEmpty()) {
                showCompletions(newText, this)
            }
        }
    }

    private fun showCompletions(newText: String, view: CommandLineView) {
        val completions = completer.completions(newText, commandLine.commands())
        view.showCompletions(completions)
    }

    /**
     * Show the completions possible in the current context on the given [view]
     */
    fun showCompletions(view: CommandLineView) {
        showCompletions(commandLine.text.now, view)
    }

    private val executeSubscription = commandLine.executed.subscribe("$this") { appl ->
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

    private fun handleState(newState: State, view: CommandLineView) {
        if (newState == EditingArgs) {
            val command = commandLine.editedCommand()
            val views = commandLine.editableArgs().map(context::createView)
            view.editingArgs(command.shortName!!, command.parameters, views)
        } else {
            view.editingName(commandLine.text.now)
        }
    }
}