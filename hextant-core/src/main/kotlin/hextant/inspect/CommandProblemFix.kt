/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import hextant.command.Command

/**
 * A [ProblemFix] that uses a [command] to fix the [Problem].
 */
class CommandProblemFix(
    override val description: String,
    private val command: Command<*, *>,
    private val arguments: List<Any?>,
    private val target: Any
) : ProblemFix {
    override fun isApplicable(): Boolean = command.isApplicableOn(target)

    @Suppress("UNCHECKED_CAST")
    override fun fix() {
        command as Command<Any, Any>
        command.execute(target, arguments)
    }
}