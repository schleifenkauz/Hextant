/**
 *@author Nikolaus Knop
 */

package hextant.command.gui

import hextant.Context
import hextant.command.Commands
import javafx.scene.control.ContextMenu

internal class CommandContextMenu<T : Any> internal constructor(
    private val target: T,
    private val commands: Commands,
    private val context: Context
) : ContextMenu() {
    init {
        setOnShowing { update() }
    }

    private fun update() {
        items.clear()
        for (c in commands.applicableOn(target)) {
            val item = CommandMenuItem(target, c, context)
            items.add(item)
        }
    }
}