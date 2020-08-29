/**
 *@author Nikolaus Knop
 */

package hextant.fx

import hextant.command.Command
import hextant.command.Commands
import hextant.command.line.CommandLine
import hextant.context.Context
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.stage.Popup

/**
 * A [Popup] that displays the commands applicable on the given [target].
 */
internal class CommandsPopup(private val context: Context, private val target: Any) : HextantPopup(context) {
    private val container = VBox().withStyleClass("command-list")

    init {
        scene.root = container
    }

    private fun commands() = context[Commands].applicableOn(target)

    override fun show() {
        val commands = commands()
        if (commands.isNotEmpty()) {
            container.children.clear()
            for (command in commands) {
                val l = Button(command.name).withStyleClass("command-item")
                l.isFocusTraversable = true
                container.children.add(l)
                l.setOnAction { expand(command) }
                l.registerShortcuts {
                    on("ENTER") { expand(command) }
                }
            }
            super.show()
        }
    }

    private fun expand(command: Command<Any, *>) {
        val cl = context[CommandLine]
        cl.expand(command)
        if (command.parameters.isEmpty()) {
            cl.execute()
        }
        hide()
    }
}