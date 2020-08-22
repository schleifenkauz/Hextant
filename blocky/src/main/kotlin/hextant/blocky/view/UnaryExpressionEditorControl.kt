/**
 *@author Nikolaus Knop
 */

package hextant.blocky.view

import bundles.Bundle
import hextant.blocky.editor.UnaryExpressionEditor
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.CompoundEditorControl

class UnaryExpressionEditorControl @ProvideImplementation(
    ControlFactory::class,
    UnaryExpressionEditor::class
) constructor(
    editor: UnaryExpressionEditor,
    args: Bundle
) : CompoundEditorControl(editor, args, {
    line {
        spacing = 2.0
        view(editor.op)
        view(editor.operand)
    }
})