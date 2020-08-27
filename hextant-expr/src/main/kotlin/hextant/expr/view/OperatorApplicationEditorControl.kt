/**
 *@author Nikolaus Knop
 */

package hextant.expr.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.CompoundEditorControl
import hextant.expr.editor.OperatorApplicationEditor

class OperatorApplicationEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    editor: OperatorApplicationEditor,
    arguments: Bundle
) : CompoundEditorControl(editor, arguments, {
    line {
        operator("(")
        view(editor.operand1)
        view(editor.operator)
        view(editor.operand2)
        operator(")")
    }
})