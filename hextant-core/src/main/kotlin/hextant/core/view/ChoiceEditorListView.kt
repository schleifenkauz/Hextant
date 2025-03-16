package hextant.core.view

import fxutils.label
import fxutils.prompt.SearchableListView
import fxutils.styleClass
import hextant.core.editor.ChoiceSource
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import reaktive.value.now

class ChoiceEditorListView<C>(
    private val editor: ChoiceSource<C>
) : SearchableListView<C>("Choose option") {
    override fun createCell(option: C): Region = HBox(label(editor.toString(option)).styleClass("option-label"))

    override fun extractText(option: C): String = editor.toString(option).now

    override fun options(): List<C> = editor.choices()
}