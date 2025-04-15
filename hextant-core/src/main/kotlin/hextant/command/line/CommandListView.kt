package hextant.command.line

import fxutils.prompt.SimpleSearchableListView
import fxutils.styleClass
import hextant.command.Command
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign2.MaterialDesignF

class CommandListView(
    title: String, commands: List<Command<*, *>>
) : SimpleSearchableListView<Command<*, *>>(commands, title) {
    override fun displayText(option: Command<*, *>): String = option.name

    override fun createCell(option: Command<*, *>): Region {
        //TODO display more infos (different icon based on command target)
        val label = Label(option.name, FontIcon(MaterialDesignF.FLASH_CIRCLE)).styleClass("option-label")
        return HBox(label)
    }

    override fun extractText(option: Command<*, *>): String = option.name
}