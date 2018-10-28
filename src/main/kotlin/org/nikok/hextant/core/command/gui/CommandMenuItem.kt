/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.command.gui

import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.INFORMATION
import javafx.scene.control.MenuItem
import org.nikok.hextant.core.command.Command
import org.nikok.hextant.core.command.CommandRegistrar
import java.util.logging.Logger

internal class CommandMenuItem<T : Any>(target: T, command: Command<T, *>, registrar: CommandRegistrar<out T>) :
        MenuItem() {
    init {
        logger.fine("New command menu item")
        text = getText(command, registrar)
        logger.fine("Set text to $text")
        if (!command.isApplicableOn(target)) {
            logger.fine { "Disabling because command is not applicable" }
            isDisable = true
        }
        setOnAction {
            executeCommand(command, target)
        }
    }

    private fun getText(
        command: Command<T, *>,
        registrar: CommandRegistrar<out T>
    ): String {
        return buildString {
            append(command.name)
            append("   ")
            val shortcut = registrar.getShortcut(command)?.toString() ?: ""
            append(shortcut)
        }
    }

    private fun executeCommand(command: Command<T, *>, target: T) {
        logger.info("Executing command $command with $target")
        if (command.parameters.isEmpty()) {
            logger.fine("No parameters just executing")
            val res = command.execute(target)
            showResult(res)
        } else {
            val owner = parentPopup.ownerWindow
            val args = showArgumentPrompt(owner, command)
            if (args != null && args.all { a -> a != null }) {
                logger.fine { "Executing command with arguments $args" }
                val res = command.execute(target, *args.toTypedArray())
                showResult(res)
            } else {
                logger.fine { "Not all arguments are ok" }
            }
        }
    }

    private fun showResult(res: Any?) {
        logger.fine { "And the result is $res" }
        Alert(INFORMATION, res.toString()).show()
    }

    companion object {
        val logger = Logger.getLogger(CommandMenuItem::class.qualifiedName)
    }
}