/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import hextant.core.editor.ChoiceEditor
import hextant.fx.EditorControl
import javafx.collections.FXCollections
import javafx.scene.control.ComboBox
import javafx.scene.control.ListCell

/**
 * JavaFX implementation of a [ChoiceEditorView]
 */
open class ChoiceEditorControl<C : Any>(
    private val items: List<C>,
    private val editor: ChoiceEditor<C>,
    arguments: Bundle
) : ChoiceEditorView<C>, EditorControl<ComboBox<C>>(editor, arguments) {
    override fun createDefaultRoot(): ComboBox<C> = ComboBox(FXCollections.observableList(items))

    init {
        root.valueProperty().addListener { _, _, item ->
            editor.select(item)
        }
        root.setCellFactory { createDefaultCell() }
    }

    private fun createDefaultCell(): ListCell<C> = object : ListCell<C>() {
        override fun updateItem(item: C?, empty: Boolean) {
            super.updateItem(item, empty)
            if (item != null || empty) {
                text = ""
                return
            }
            text = item.toString()
        }
    }

    override fun selected(choice: C) {
        root.selectionModel.select(choice)
    }
}