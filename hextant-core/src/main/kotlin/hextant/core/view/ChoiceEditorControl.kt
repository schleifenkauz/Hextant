/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import bundles.publicProperty
import fxutils.button
import fxutils.children
import fxutils.withStyleClass
import hextant.context.ControlFactory
import hextant.context.createControl
import hextant.core.Editor
import hextant.core.editor.ChoiceEditor
import hextant.core.editor.SimpleEditor
import hextant.core.view.ChoiceEditorControl.Layout.Horizontal
import hextant.core.view.ChoiceEditorControl.Layout.Vertical
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import reaktive.value.fx.asObservableValue
import reaktive.value.now

/**
 * JavaFX implementation of a [ChoiceEditorView]
 */
open class ChoiceEditorControl<C : Any, E : Editor<*>>(
    val editor: ChoiceEditor<C, *, E>,
    arguments: Bundle
) : ChoiceEditorView<C, E>, WrappingEditorControl<Pane>(editor, arguments) {
    var canChoose = true

    val button = button(style = "selector-button") {
        if (canChoose) {
            val choice = ChoiceEditorSelectorPrompt(editor)
                .showPopup(anchorNode = this, initialOption = editor.selected.now)
            if (choice != null) editor.select(choice)
        }
    }

    init {
        editor.addView(this)
        addArgumentHandler(LAYOUT) {
            root = createDefaultRoot()
        }
    }

    override fun selected(choice: C, content: E) {
        if (button.textProperty().isBound) button.textProperty().unbind()
        button.textProperty().bind(editor.toString(choice).asObservableValue())
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
        +button
        wrapped?.let { +it }
    }

    enum class Layout {
        Horizontal, Vertical
    }

    companion object : ControlFactory<ChoiceEditor<*, *, *>> {
        val LAYOUT = publicProperty("LAYOUT", Horizontal)

        override fun createControl(editor: ChoiceEditor<*, *, *>, arguments: Bundle): EditorControl<*> =
            ChoiceEditorControl(editor, arguments)
    }
}