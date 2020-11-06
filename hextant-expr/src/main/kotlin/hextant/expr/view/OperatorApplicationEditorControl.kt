/**
 *@author Nikolaus Knop
 */

package hextant.expr.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.CompoundEditorControl
import hextant.expr.editor.OperatorApplicationEditor
import hextant.fx.createBorder
import hextant.settings.Settings
import javafx.scene.paint.Color
import reaktive.value.binding.map
import reaktive.value.fx.asObservableValue

class OperatorApplicationEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    editor: OperatorApplicationEditor,
    arguments: Bundle
) : CompoundEditorControl(editor, arguments, {
    line {
        view(editor.operand1)
        view(editor.operator)
        view(editor.operand2)
    }

    val border = editor.context[Settings][Style.BorderColor].map { c -> createBorder(Color.web(c), 2.0) }
    borderProperty().bind(border.asObservableValue())
})