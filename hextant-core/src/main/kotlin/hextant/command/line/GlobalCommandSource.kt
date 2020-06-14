/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.Context
import hextant.command.Command
import hextant.command.Commands

internal class GlobalCommandSource(private val platform: Context) : CommandSource {
    override fun focusedTarget(): Any? = platform

    override fun selectedTargets(): Collection<Any> = listOf(platform)

    override fun commandsFor(target: Any): Collection<Command<*, *>> = platform[Commands].applicableOn(target)
}