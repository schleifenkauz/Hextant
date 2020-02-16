/**
 * @author Nikolaus Knop
 */

package hextant.command.line

import hextant.Editor
import hextant.EditorView
import hextant.command.Command

interface CommandLineView : EditorView {
    fun displayCommandName(name: String)

    fun expanded(command: Command<*, *>, editors: List<Editor<*>>)

    fun reset()

    fun addToHistory(command: Command<*, *>, arguments: List<Any>, result: Any)
}