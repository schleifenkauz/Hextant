/**
 * @author Nikolaus Knop
 */

package hextant.command.line

import hextant.Editor
import hextant.EditorView
import hextant.command.Command

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
     * Add an application of [command] to the given [arguments] with the given [result] to the execution history.
     */
    fun addToHistory(command: Command<*, *>, arguments: List<Any>, result: Any)
}