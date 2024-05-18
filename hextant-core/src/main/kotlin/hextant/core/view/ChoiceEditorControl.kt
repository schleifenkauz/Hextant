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
import hextant.fx.ComboBoxConfig
import hextant.fx.children
import hextant.fx.withStyleClass
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import reaktive.value.now

/**
 * JavaFX implementation of a [ChoiceEditorView]
 */
open class ChoiceEditorControl<C : Any, E : Editor<*>>(
    val editor: ChoiceEditor<C, *, E>,
    arguments: Bundle
) : ChoiceEditorView<C, E>, WrappingEditorControl<Pane>(editor, arguments) {
    private val comboBox = ComboBoxConfig.createComboBox(editor, arguments)

    init {
        editor.addView(this)
    }

    override fun <T : Any> argumentChanged(property: Property<T, *>, value: T) {
        when (property) {
            LAYOUT -> root = createDefaultRoot()
            ComboBoxConfig.searchable -> ComboBoxConfig.setSearchable(comboBox, value as Boolean)
        }
    }

    override fun selected(choice: C, content: E) {
        if (comboBox.value != choice) {
            comboBox.selectionModel.select(choice)
        }
        wrapped = if (content !is SimpleEditor<*>) {
            context.createControl(editor.content.now).withStyleClass("choice-editor-content")
        } else null
        root = createDefaultRoot()
        wrapped?.receiveFocus()
    }

    override fun createDefaultRoot(): Pane = when (arguments[LAYOUT]) {
        Horizontal -> HBox().withStyleClass("horizontal-choice-editor")
        Vertical -> VBox().withStyleClass("vertical-choice-editor")
    }.withStyleClass("choice-editor").children {
        +comboBox
        wrapped?.let { +it }
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