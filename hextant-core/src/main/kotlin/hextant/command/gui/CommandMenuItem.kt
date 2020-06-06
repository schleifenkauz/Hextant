/**
 *@author Nikolaus Knop
 */

package hextant.command.gui

import hextant.Context
import hextant.command.Command
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.INFORMATION
import javafx.scene.control.MenuItem

internal class CommandMenuItem<T : Any>(target: T, command: Command<T, *>, private val context: Context) : MenuItem() {
    init {
        text = getText(command)
        if (!command.isApplicableOn(target)) {
            isDisable = true
        }
        setOnAction {
            executeCommand(command, target)
        }
    }

    private fun getText(command: Command<T, *>): String {
        return buildString {
            append(command.name)
        }
    }

    private fun executeCommand(command: Command<T, *>, target: T) {
        if (command.parameters.isEmpty()) {
            val res = command.execute(target, emptyList())
            showResult(res)
        } else {
            val owner = parentPopup.ownerWindow
            val args = showArgumentPrompt(owner, command, context)
            if (args != null && args.all { a -> a != null }) {
                val res = command.execute(target, args)
                showResult(res)
            }
        }
    }

    private fun showResult(res: Any?) {
        Alert(INFORMATION, res.toString()).show()
    }
}