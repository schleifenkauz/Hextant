/**
 * @author Nikolaus Knop
 */

package hextant.command.meta

annotation class ProvideCommand(
    val name: String = DEFAULT,
    val shortName: String = DEFAULT,
    val category: String = DEFAULT,
    val description: String = DEFAULT
)

internal const val DEFAULT = "<default>"

annotation class CommandParameter(val name: String = DEFAULT, val description: String = DEFAULT)

