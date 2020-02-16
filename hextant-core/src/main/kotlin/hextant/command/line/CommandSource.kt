/**
 * @author Nikolaus Knop
 */

package hextant.command.line

import hextant.command.Command

/**
 * A command source is used by the [CommandLine] to query the applicable commands.
 */
interface CommandSource {
    /**
     * Returns the target, that was focused most recently or `null` if no target is focused.
     */
    fun focusedTarget(): Any?

    /**
     * Returns a collection of targets that are currently selected.
     */
    fun selectedTargets(): Collection<Any>

    /**
     * Returns all the commands applicable on the given [target].
     */
    fun commandsFor(target: Any): Collection<Command<*, *>>
}