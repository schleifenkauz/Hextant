package hextant.core.view

import fxutils.prompt.SearchableListView
import fxutils.styleClass
import hextant.core.editor.ChoiceSource
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Region

class ChoiceEditorListView<C : Any>(
    private val editor: ChoiceSource<C>
) : SearchableListView<C>("Choose option") {
    override fun createCell(option: C): Region = HBox(Label(displayText(option)).styleClass("option-label"))

    override fun extractText(option: C): String = editor.toString(option)

    override fun options(): List<C> = editor.choices()
}