/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.Context
import hextant.command.*
import hextant.get
import hextant.impl.SelectionDistributor
import reaktive.value.now

/**
 * A [CommandSource] the uses the [SelectionDistributor] and the [Commands] of the given [context].
 */
class ContextCommandSource(private val context: Context) : CommandSource {
    override fun focusedTarget(): Any? = context[SelectionDistributor].selectedTarget.now

    override fun selectedTargets(): Collection<Any> = context[SelectionDistributor].selectedTargets.now

    override fun commandsFor(target: Any): Collection<Command<*, *>> = context[Commands].applicableOn(target)
}