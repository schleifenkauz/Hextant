/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.command.gui

import javafx.scene.control.MenuItem
import org.nikok.hextant.core.command.Command
import org.nikok.hextant.core.command.CommandRegistrar

internal class CommandMenuItem<T : Any>(target: T, command: Command<T, *>, registrar: CommandRegistrar<out T>) :
        MenuItem() {
    init {
        text = buildString {
            append(command.name)
            append("   ")
            val shortcut = registrar.getShortcut(command)?.toString() ?: ""
            append(shortcut)
        }
        if (!command.isApplicableOn(target)) {
            isDisable = true
        }
        setOnAction {
            if (command.parameters.isEmpty()) {
                command.execute(target)
            } else {
                val owner = parentPopup.ownerWindow
                val args = showArgumentPrompt(owner, command)
                if (args != null && args.all { a -> a != null }) {
                    command.execute(target, *args.toTypedArray())
                }
            }
        }
    }
}