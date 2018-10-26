/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.command.gui

import javafx.scene.control.ContextMenu
import org.nikok.hextant.core.command.CommandRegistrar

internal class CommandContextMenu<T : Any> internal constructor(
    private val target: T,
    private val commandRegistrar: CommandRegistrar<T>
) : ContextMenu() {
    init {
        update()
        setOnShowing { update() }
    }

    private fun update() {
        items.clear()
        for (c in commandRegistrar.commands) {
            val item = CommandMenuItem(target, c, commandRegistrar)
            items.add(item)
        }
    }
}