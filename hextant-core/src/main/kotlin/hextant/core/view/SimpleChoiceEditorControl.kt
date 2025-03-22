package hextant.core.view

import bundles.Bundle
import fxutils.button
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.editor.SimpleChoiceEditor
import javafx.scene.control.Button
import reaktive.value.fx.asObservableValue
import reaktive.value.now

open class SimpleChoiceEditorControl<C>(
    val editor: SimpleChoiceEditor<C>,
    arguments: Bundle
) : SimpleChoiceEditorView<C>, EditorControl<Button>(editor, arguments) {
    init {
        editor.addView(this)
    }

    override fun createDefaultRoot() = button { showChoicePopup() }

    protected open fun showChoicePopup() {
        ChoiceEditorListView(editor).showPopup(anchorNode = this, initialOption = editor.result.now) { option ->
            editor.select(option)
        }
    }

    override fun selected(choice: C) {
        if (root.textProperty().isBound) root.textProperty().unbind()
        root.textProperty().bind(editor.toString(choice).asObservableValue())
    }

    @ProvideImplementation(ControlFactory::class)
    companion object : ControlFactory<SimpleChoiceEditor<*>> {
        override fun createControl(editor: SimpleChoiceEditor<*>, arguments: Bundle): EditorControl<*> =
            SimpleChoiceEditorControl(editor, arguments)
    }
}

