/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.command.line

import org.nikok.hextant.core.command.Command
import org.nikok.hextant.core.completion.*

internal object CommandCompletionFactory: CompletionFactory<Command<*, *>> {
    override fun getCompletion(completed: Command<*, *>): Completion<Command<*, *>> {
        val tooltipText = completed.toString()
        val text = "${completed.name} (${completed.shortName})"
        return TextCompletion(completed, text, tooltipText)
    }
}