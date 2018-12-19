/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.command.gui

import javafx.scene.control.ContextMenu
import org.nikok.hextant.Context
import org.nikok.hextant.core.command.CommandRegistrar
import org.nikok.hextant.core.impl.myLogger

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