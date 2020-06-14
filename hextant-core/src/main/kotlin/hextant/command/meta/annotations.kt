/**
 * @author Nikolaus Knop
 */

package hextant.command.meta

import hextant.command.Command


/**
 * This annotation can be used on a member function to register a command that executes this function.
 * @constructor Instantiates this annotation, you can use `"<default>"` for the string parameters to use the default value.
 * @property name The [Command.name] of the registered command, defaults to the functions name
 * @property shortName The [Command.shortName] of the registered command, defaults to the function name
 * @property category The [Command.category] of the registered command, defaults to `null`
 * @property defaultShortcut the shortcut that triggers this command
 * @property description The [Command.description] of the registered command, defaults to `"No description provided"`
 * @property type The [Command.Type] of the registered command
 * @property initiallyEnabled indicates whether the command should be enabled by default
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ProvideCommand(
    val name: String = DEFAULT,
    val shortName: String = DEFAULT,
    val category: String = DEFAULT,
    val defaultShortcut: String = DEFAULT,
    val description: String = DEFAULT,
    val type: Command.Type = Command.Type.MultipleReceivers,
    val initiallyEnabled: Boolean = true
)
internal const val DEFAULT = "<default>"

/**
 * This annotation can be used on a parameter of a member function with the [ProvideCommand] to configure a command parameter.
 * @constructor Instantiates this annotation, you can use `"<default>"` for the string parameters to use the default value.
 * @property name The [Command.Parameter.name] of the parameter, defaults to the name of the function parameter
 * @property description The [Command.Parameter.description] of the parameter, defaults to `"No description provided"`
 */
annotation class CommandParameter(val name: String = DEFAULT, val description: String = DEFAULT)