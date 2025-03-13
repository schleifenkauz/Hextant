package hextant.core.view

import bundles.Bundle
import fxutils.button
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.editor.SimpleChoiceEditor
import javafx.scene.control.Button
import reaktive.value.now

open class SimpleChoiceEditorControl<C : Any>(
    val editor: SimpleChoiceEditor<C>,
    arguments: Bundle
) : SimpleChoiceEditorView<C>, EditorControl<Button>(editor, arguments) {
    private val listView = ChoiceEditorListView(editor)
    private val button = button {
        listView.showPopup(anchorNode = this, initialOption = editor.result.now) { option ->
            editor.select(option)
        }
    }

    init {
        editor.addView(this)
    }

    override fun createDefaultRoot() = button

    override fun selected(choice: C) {
        root.text = editor.toString(choice)
    }

    @ProvideImplementation(ControlFactory::class)
    companion object : ControlFactory<SimpleChoiceEditor<*>> {
        override fun createControl(editor: SimpleChoiceEditor<*>, arguments: Bundle): EditorControl<*> =
            SimpleChoiceEditorControl(editor, arguments)
    }
}

