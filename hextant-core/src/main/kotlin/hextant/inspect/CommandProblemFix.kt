/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import hextant.command.Command

/**
 * A [ProblemFix] that uses a [command] to fix the [Problem].
 */
class CommandProblemFix<T : Any>(
    override val description: String,
    private val command: Command<*, *>,
    private val arguments: List<Any>,
    private val target: InspectionBody<T>.() -> Any
) : ProblemFix<T> {
    override fun InspectionBody<T>.isApplicable(): Boolean = command.isApplicableOn(target())

    @Suppress("UNCHECKED_CAST")
    override fun InspectionBody<T>.fix() {
        command as Command<Any, Any>
        command.execute(target(), arguments)
    }
}