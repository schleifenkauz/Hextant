package hextant.core.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.editor.SimpleChoiceEditor
import hextant.fx.withStyleClass
import javafx.collections.FXCollections.observableList
import javafx.scene.control.ComboBox
import javafx.scene.control.ListCell
import javafx.util.StringConverter
import org.controlsfx.control.SearchableComboBox

open class SimpleChoiceEditorControl<C : Any>(
    val editor: SimpleChoiceEditor<C>,
    arguments: Bundle
) : SimpleChoiceEditorView<C>, EditorControl<ComboBox<C>>(editor, arguments) {
    init {
        root.valueProperty().addListener { _, _, item ->
            if (item != null) editor.select(item)
        }
        root.setCellFactory { createDefaultCell() }
        root.converter = object : StringConverter<C>() {
            override fun toString(item: C?): String = if (item != null) editor.toString(item) else "<null>"

            override fun fromString(str: String?): C? = if (str != null) editor.fromString(str) else null
        }
        editor.addView(this)
    }

    override fun createDefaultRoot() = SearchableComboBox(observableList(editor.choices()))
        .withStyleClass("choice-editor-combo-box")

    private fun createDefaultCell(): ListCell<C> = object : ListCell<C>() {
        override fun updateItem(item: C?, empty: Boolean) {
            super.updateItem(item, empty)
            if (item == null || empty) {
                text = ""
                return
            }
            text = editor.toString(item)
        }
    }

    override fun selected(choice: C) {
        if (root.value != choice) {
            root.selectionModel.select(choice)
        }
    }


    @ProvideImplementation(ControlFactory::class)
    companion object : ControlFactory<SimpleChoiceEditor<*>> {
        override fun createControl(editor: SimpleChoiceEditor<*>, arguments: Bundle): EditorControl<*> =
            SimpleChoiceEditorControl(editor, arguments)
    }
}