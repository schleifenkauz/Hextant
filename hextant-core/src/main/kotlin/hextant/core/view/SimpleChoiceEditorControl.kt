package hextant.core.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.editor.SimpleChoiceEditor
import hextant.fx.ComboBoxConfig
import javafx.scene.control.ComboBox

open class SimpleChoiceEditorControl<C : Any>(
    val editor: SimpleChoiceEditor<C>,
    arguments: Bundle
) : SimpleChoiceEditorView<C>, EditorControl<ComboBox<C>>(editor, arguments) {
    init {
        editor.addView(this)
    }

    override fun createDefaultRoot() = ComboBoxConfig.createComboBox(editor, arguments)

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