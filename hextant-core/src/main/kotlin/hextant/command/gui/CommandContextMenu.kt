/**
 *@author Nikolaus Knop
 */

package hextant.command.gui

import hextant.Context
import hextant.command.CommandRegistrar
import hextant.impl.myLogger
import javafx.scene.control.ContextMenu

internal class CommandContextMenu<T : Any> internal constructor(
    private val target: T,
    private val commandRegistrar: CommandRegistrar<T>,
    private val context: Context
) : ContextMenu() {
    init {
        update()
        setOnShowing { update() }
    }

    private fun update() {
        items.clear()
        for (c in commandRegistrar.commands) {
            val item = CommandMenuItem(target, c, commandRegistrar, context)
            items.add(item)
        }
    }

    companion object {
        val logger by myLogger()
    }
}