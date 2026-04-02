package hextant.core.view

import bundles.Bundle
import fxutils.escapeUnderscores
import fxutils.registerShortcuts
import fxutils.selectorButton
import hextant.context.ControlFactory
import hextant.core.editor.SimpleChoiceEditor
import javafx.scene.control.Button
import javafx.scene.layout.Region
import reaktive.value.binding.map
import reaktive.value.fx.asObservableValue
import reaktive.value.now

open class SimpleChoiceEditorControl<C : Any>(
    val editor: SimpleChoiceEditor<C>,
    arguments: Bundle
) : SimpleChoiceEditorView<C>, EditorControl<Region>(editor, arguments) {
    protected val selectorButton = selectorButton { showChoicePopup() }

    init {
        editor.addView(this)
        registerShortcuts {
            on("Enter") { ev ->
                showChoicePopup()
                ev.consume()
            }
        }
    }

    override fun createDefaultRoot(): Region = selectorButton

    protected open fun showChoicePopup() {
        val choice = ChoiceEditorSelectorPrompt(editor).showPopup(anchorNode = this, initialOption = editor.result.now)
        if (choice != null) editor.select(choice)
    }

    override fun receiveFocus() {
        selectorButton.requestFocus()
    }

    override fun selected(choice: C) {
        if (selectorButton.textProperty().isBound) selectorButton.textProperty().unbind()
        selectorButton.textProperty().bind(editor.toString(choice).map { txt -> txt.escapeUnderscores() }.asObservableValue())
    }

    companion object : ControlFactory<SimpleChoiceEditor<*>> {
        override fun createControl(editor: SimpleChoiceEditor<*>, arguments: Bundle): EditorControl<*> =
            SimpleChoiceEditorControl(editor, arguments)
    }
}

