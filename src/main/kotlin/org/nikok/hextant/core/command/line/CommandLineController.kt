/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.command.line

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.EditorViewFactory
import org.nikok.hextant.core.base.AbstractEditor
import org.nikok.hextant.core.command.line.CommandLine.State
import org.nikok.hextant.core.command.line.CommandLine.State.EditingArgs
import org.nikok.hextant.get
import org.nikok.reaktive.event.subscribe
import org.nikok.reaktive.value.now
import org.nikok.reaktive.value.observe

/**
 * The controller for a [CommandLine]
 * @constructor
 * @param commandLine the [CommandLine] controlled by this controller
 * @param view the view using this controller
 * @param viewFactory the [EditorViewFactory] used to get the views for the argument editors
 */
class CommandLineController(
    val commandLine: CommandLine,
    view: CommandLineView,
    platform: HextantPlatform
): AbstractEditor<CommandLine, CommandLineView>(commandLine, platform) {
    private val viewFactory = platform[EditorViewFactory]

    private val completer = CommandCompleter

    private val stateObserver = commandLine.state.observe("Observe state of $commandLine") { newState ->
        handleState(newState, view)
    }

    private val textObserver = commandLine.text.observe("Observe text of $commandLine") { newText ->
        view.setText(newText)
        if (newText.isNotEmpty()) {
            showCompletions(newText, view)
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
        view.executed(appl)
    }

    fun dispose() {
        stateObserver.kill()
        textObserver.kill()
        executeSubscription.cancel()
    }

    init {
        handleState(commandLine.state.now, view)
    }

    private fun handleState(newState: State, view: CommandLineView) {
        if (newState == EditingArgs) {
            val command = commandLine.editedCommand()
            val views = commandLine.editableArgs().map(viewFactory::getFXView)
            view.editingArgs(command.shortName!!, command.parameters, views)
        } else {
            view.editingName(commandLine.text.now)
        }
    }
}