/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.command.Command
import hextant.completion.*

internal object CommandCompletionFactory: CompletionFactory<Command<*, *>> {
    override fun getCompletion(completed: Command<*, *>): Completion<Command<*, *>> {
        val tooltipText = completed.toString()
        val text = "${completed.name} (${completed.shortName})"
        return TextCompletion(completed, text, tooltipText)
    }
}