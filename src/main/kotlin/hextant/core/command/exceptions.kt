/**
 * @author Nikolaus Knop
 */

package hextant.core.command

/**
 * Thrown when a [Command] is executed with the wrong count or type of arguments
*/
class ArgumentMismatchException(message: String? = null, cause: Throwable? = null)
    : IllegalArgumentException(message, cause)