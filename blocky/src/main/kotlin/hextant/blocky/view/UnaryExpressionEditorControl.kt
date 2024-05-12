/**
 *@author Nikolaus Knop
 */

package hextant.blocky.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.CompoundEditorControl

class UnaryExpressionEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    private val editor: hextant.blocky.editor.UnaryExpressionEditor, arguments: Bundle
) : CompoundEditorControl(editor, arguments) {
    override fun build(): Layout = horizontal {
        spacing = 2.0
        view(editor.op)
        view(editor.operand)
    }

}