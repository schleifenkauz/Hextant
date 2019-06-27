/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.Editor
import hextant.EditorView
import hextant.command.Command.Parameter

/**
 * View for a [CommandLine]
 */
interface CommandLineView: EditorView {
    /**
     * Called when the associated [CommandLine]s state switched to "editing args"
     * @param name the name of the expanded command
     * @param parameters the parameters of the command
     * @param editors the argument editors
     */
    fun editingArguments(
        name: String,
        parameters: List<Parameter>,
        editors: List<Editor<*>>
    )

    /**
     * Called when the associated [CommandLine]s state switched to "editing name"
     * @param name the name of the edited command
     */
    fun editingName(name: String)

    /**
     * Called when the associated [CommandLine]s text changed to [newText]
     */
    fun displayText(newText: String)

    /**
     * Called when the associated [CommandLine] executed the given command application [appl]
     */
    fun executed(appl: CommandApplication)
}