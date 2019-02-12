/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.command.Command
import hextant.completion.*
import hextant.completion.CompletionResult.Match

internal object CommandCompletionFactory: CompletionFactory<Command<*, *>> {
    override fun getCompletion(
        match: Match,
        completedText: String,
        completed: Command<*, *>
    ): Completion<Command<*, *>> {
        val tooltipText = completed.toString()
        val text = "${completed.name} (${completed.shortName})"
        return TextCompletion(match, completed, text, tooltipText)
    }
}