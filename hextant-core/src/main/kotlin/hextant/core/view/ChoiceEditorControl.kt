/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import bundles.Property
import bundles.publicProperty
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.context.createControl
import hextant.core.Editor
import hextant.core.editor.ChoiceEditor
import hextant.core.editor.SimpleEditor
import hextant.core.view.ChoiceEditorControl.Layout.Horizontal
import hextant.core.view.ChoiceEditorControl.Layout.Vertical
import hextant.fx.children
import hextant.fx.withStyleClass
import javafx.collections.FXCollections.observableList
import javafx.scene.Node
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.util.StringConverter
import org.controlsfx.control.SearchableComboBox
import reaktive.value.now

/**
 * JavaFX implementation of a [ChoiceEditorView]
 */
open class ChoiceEditorControl<C : Any, E : Editor<*>>(
    val editor: ChoiceEditor<C, *, E>,
    arguments: Bundle
) : ChoiceEditorView<C, E>, EditorControl<Pane>(editor, arguments) {
    private val comboBox = SearchableComboBox(observableList(editor.choices()))
        .withStyleClass("choice-editor-combo-box")
    private var contentEditor: Node? = context.createControl(editor.content.now)
        .withStyleClass("choice-editor-content")

    init {
        comboBox.valueProperty().addListener { _, _, item ->
            if (item != null) editor.select(item)
        }
        comboBox.converter = object : StringConverter<C>() {
            override fun toString(item: C?): String = if (item != null) editor.toString(item) else "<null>"

            override fun fromString(str: String?): C? = if (str != null) editor.fromString(str) else null
        }
        editor.addView(this)
    }

    override fun <T : Any> argumentChanged(property: Property<T, *>, value: T) {
        when (property) {
            LAYOUT -> root = createDefaultRoot()
        }
    }

    override fun selected(choice: C, content: E) {
        if (comboBox.value != choice) {
            comboBox.selectionModel.select(choice)
        }
        contentEditor = if (content !is SimpleEditor<*>) {
            context.createControl(editor.content.now)
        } else null
        root = createDefaultRoot()
    }

    override fun createDefaultRoot(): Pane = when (arguments[LAYOUT]) {
        Horizontal -> HBox().withStyleClass("horizontal-choice-editor")
        Vertical -> VBox().withStyleClass("vertical-choice-editor")
    }.withStyleClass("choice-editor").children {
        +comboBox
        contentEditor?.let { +it }
    }

    enum class Layout {
        Horizontal, Vertical
    }

    @ProvideImplementation(ControlFactory::class)
    companion object : ControlFactory<ChoiceEditor<*, *, *>> {
        val LAYOUT = publicProperty("LAYOUT", Horizontal)

        override fun createControl(editor: ChoiceEditor<*, *, *>, arguments: Bundle): EditorControl<*> =
            ChoiceEditorControl(editor, arguments)
    }
}