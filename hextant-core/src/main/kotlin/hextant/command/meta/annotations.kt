/**
 * @author Nikolaus Knop
 */

package hextant.command.meta

import hextant.command.Command

annotation class ProvideCommand(
    val name: String = DEFAULT,
    val shortName: String = DEFAULT,
    val category: String = DEFAULT,
    val description: String = DEFAULT,
    val type: Command.Type = Command.Type.MultipleReceivers
)

internal const val DEFAULT = "<default>"

annotation class CommandParameter(val name: String = DEFAULT, val description: String = DEFAULT)