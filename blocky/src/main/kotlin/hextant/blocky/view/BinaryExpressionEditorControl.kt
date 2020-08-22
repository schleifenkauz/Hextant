/**
 *@author Nikolaus Knop
 */

package hextant.blocky.view

import bundles.Bundle
import hextant.blocky.editor.BinaryExpressionEditor
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.CompoundEditorControl

class BinaryExpressionEditorControl @ProvideImplementation(
    ControlFactory::class,
    BinaryExpressionEditor::class
) constructor(editor: BinaryExpressionEditor, args: Bundle) :
    CompoundEditorControl(editor, args, {
        line {
            spacing = 2.0
            view(editor.left)
            view(editor.op)
            view(editor.right)
        }
    })