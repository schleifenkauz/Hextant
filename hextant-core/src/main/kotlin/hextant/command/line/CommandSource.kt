/**
 * @author Nikolaus Knop
 */

package hextant.command.line

import hextant.command.Command

interface CommandSource {
    fun focusedTarget(): Any?

    fun selectedTargets(): Collection<Any>

    fun commandsFor(target: Any): Collection<Command<*, *>>
}