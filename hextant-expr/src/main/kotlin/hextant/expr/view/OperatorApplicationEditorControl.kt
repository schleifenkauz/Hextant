/**
 *@author Nikolaus Knop
 */

package hextant.expr.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.config.Settings
import hextant.context.ControlFactory
import hextant.core.view.CompoundEditorControl
import hextant.expr.editor.OperatorApplicationEditor
import hextant.fx.createBorder
import reaktive.value.binding.map
import reaktive.value.fx.asObservableValue

class OperatorApplicationEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    private val editor: OperatorApplicationEditor,
    arguments: Bundle
) : CompoundEditorControl(editor, arguments) {
    override fun build(): Layout = horizontal {
        view(editor.operand1)
        view(editor.operator)
        view(editor.operand2)
        val border = editor.context[Settings].getReactive(Style.BorderColor).map { c -> createBorder(c, 2.0) }
        root.borderProperty().bind(border.asObservableValue())
    }
}