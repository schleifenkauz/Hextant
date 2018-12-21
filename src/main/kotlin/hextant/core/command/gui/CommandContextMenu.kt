/**
 *@author Nikolaus Knop
 */

package hextant.core.command.gui

import hextant.Context
import hextant.core.command.CommandRegistrar
import hextant.core.impl.myLogger
import javafx.scene.control.ContextMenu

internal class CommandContextMenu<T : Any> internal constructor(
    private val target: T,
    private val commandRegistrar: CommandRegistrar<T>,
    private val context: Context
) : ContextMenu() {
    init {
        logger.info("New Command context menu")
        update()
        setOnShowing { update() }
    }

    private fun update() {
        logger.info("updating")
        items.clear()
        for (c in commandRegistrar.commands) {
            logger.finest { "Showing command $c" }
            val item = CommandMenuItem(target, c, commandRegistrar, context)
            items.add(item)
        }
    }

    companion object {
        val logger by myLogger()
    }
}