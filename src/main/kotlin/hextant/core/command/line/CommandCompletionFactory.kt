/**
 *@author Nikolaus Knop
 */

package hextant.core.command.line

import hextant.core.command.Command
import hextant.core.completion.*

internal object CommandCompletionFactory: CompletionFactory<Command<*, *>> {
    override fun getCompletion(completed: Command<*, *>): Completion<Command<*, *>> {
        val tooltipText = completed.toString()
        val text = "${completed.name} (${completed.shortName})"
        return TextCompletion(completed, text, tooltipText)
    }
}