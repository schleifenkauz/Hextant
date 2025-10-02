package hextant.core.view

import bundles.Bundle
import fxutils.escapeUnderscores
import fxutils.selectorButton
import hextant.context.ControlFactory
import hextant.core.editor.SimpleChoiceEditor
import javafx.scene.control.Button
import reaktive.value.binding.map
import reaktive.value.fx.asObservableValue
import reaktive.value.now

open class SimpleChoiceEditorControl<C : Any>(
    val editor: SimpleChoiceEditor<C>,
    arguments: Bundle
) : SimpleChoiceEditorView<C>, EditorControl<Button>(editor, arguments) {
    init {
        editor.addView(this)
        //TODO how can we have this behave better?
        //root.focusedProperty().addListener { _, _, focused -> if (focused) showChoicePopup() }
    }

    override fun createDefaultRoot() = selectorButton { showChoicePopup() }

    protected open fun showChoicePopup() {
        val choice = ChoiceEditorSelectorPrompt(editor).showPopup(anchorNode = this, initialOption = editor.result.now)
        if (choice != null) editor.select(choice)
    }

    override fun selected(choice: C) {
        if (root.textProperty().isBound) root.textProperty().unbind()
        root.textProperty().bind(editor.toString(choice).map { txt -> txt.escapeUnderscores() }.asObservableValue())
    }

    companion object : ControlFactory<SimpleChoiceEditor<*>> {
        override fun createControl(editor: SimpleChoiceEditor<*>, arguments: Bundle): EditorControl<*> =
            SimpleChoiceEditorControl(editor, arguments)
    }
}

