/**
 * @author Nikolaus Knop
 */

package hextant.command.line

import hextant.command.Command
import hextant.command.line.CommandLine.HistoryItem
import hextant.core.Editor
import hextant.core.EditorView

/**
 * Displays a [CommandLine]
 */
interface CommandLineView : EditorView {
    /**
     * Display the given command [name].
     */
    fun displayCommandName(name: String)

    /**
     * Called when the [CommandLine] was expanded.
     */
    fun expanded(command: Command<*, *>, editors: List<Editor<*>>)

    /**
     * Called when the [CommandLine] was reset.
     */
    fun reset()

    /**
     * Add the given [item] to the history.
     */
    fun addToHistory(item: HistoryItem)
}