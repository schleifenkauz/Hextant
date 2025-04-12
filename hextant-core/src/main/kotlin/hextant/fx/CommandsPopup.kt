/**
 *@author Nikolaus Knop
 */

package hextant.fx

import fxutils.registerShortcuts
import fxutils.withStyleClass
import hextant.command.Command
import hextant.command.Commands
import hextant.context.Context
import hextant.core.view.EditorControl
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.stage.Popup

/**
 * A [Popup] that displays the commands applicable on the given [target].
 */
internal class CommandsPopup(
    private val context: Context,
    private val control: EditorControl<*>,
    private val target: Any
) : HextantPopup(context) {
    private val container = VBox().withStyleClass("command-list")

    init {
        scene.root = container
    }

    override fun show() { //TODO make this more stylish
        val onEditor = context[Commands].applicableOn(target)
        val onControl = context[Commands].applicableOn(control)
        val commands: List<Command<*, *>> = (onEditor + onControl)
            .filter { cmd -> cmd.parameters.isEmpty() }
        fun execute(command: Command<*, *>) {
            command as Command<Any, *>
            when (command) {
                in onEditor -> command.execute(target, emptyList())
                in onControl -> command.execute(control, emptyList())
            }
        }
        if (commands.isNotEmpty()) {
            container.children.clear()
            for (command in commands) {
                val l = Button(command.name).withStyleClass("command-item")
                l.isFocusTraversable = true
                container.children.add(l)
                l.setOnAction { execute(command) }
                l.registerShortcuts {
                    on("ENTER") { execute(command) }
                }
            }
            super.show()
        }
    }
}