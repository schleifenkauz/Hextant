/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.command.line

import org.nikok.hextant.EditorView
import org.nikok.hextant.core.base.EditorControl
import org.nikok.hextant.core.command.Command
import org.nikok.hextant.core.command.Command.Parameter
import org.nikok.hextant.core.completion.Completion
import org.nikok.hextant.core.fx.FXEditorView

interface CommandLineView: EditorView {
    /**
     * Called when the associated [CommandLine]s state switched to "editing args"
     * @param name the name of the expanded command
     * @param parameters the parameters of the command
     * @param views the [FXEditorView]s of the argument editors
    */
    fun editingArgs(
        name: String,
        parameters: List<Parameter>,
        views: List<EditorControl<*>>
    )

    /**
     * Called when the associated [CommandLine]s state switched to "editing name"
     * @param name the name of the edited command
    */
    fun editingName(name: String)

    /**
     * Called when the associated [CommandLine]s text changed to [newText]
    */
    fun setText(newText: String)

    /**
     * Show the given [completions] on this view
    */
    fun showCompletions(completions: Set<Completion<Command<*, *>>>)

    /**
     * Called when the associated [CommandLine] executed the given command application [appl]
    */
    fun executed(appl: CommandApplication<Any>)
}