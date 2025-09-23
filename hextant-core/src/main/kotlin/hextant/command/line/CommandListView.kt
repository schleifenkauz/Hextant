package hextant.command.line

import fxutils.infiniteSpace
import fxutils.prompt.SimpleSelectorPrompt
import fxutils.styleClass
import hextant.command.Command
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign2.MaterialDesignF

class CommandListView(
    title: String, commands: List<Command<*, *>>
) : SimpleSelectorPrompt<Command<*, *>>(commands, title) {
    override fun displayText(option: Command<*, *>): String = option.name

    override fun createCell(option: Command<*, *>): Region {
        //TODO display more infos (different icon based on command target)
        val nameLabel = Label(option.name, FontIcon(MaterialDesignF.FLASH_CIRCLE)).styleClass("option-label")
        val space = infiniteSpace()
        space.minWidth = 20.0
        val shortcutLabel = Label(option.shortcut?.toString()).styleClass("shortcut-label")
        return HBox(nameLabel, space, shortcutLabel)
    }

    override fun extractText(option: Command<*, *>): String = option.name
}