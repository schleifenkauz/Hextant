/**
 *@author Nikolaus Knop
 */

package hextant.blocky.view

import bundles.Bundle
import hextant.blocky.editor.AssignEditor
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.CompoundEditorControl

class AssignEditorControl @ProvideImplementation(
    ControlFactory::class,
    AssignEditor::class
) constructor(editor: AssignEditor, args: Bundle) :
    CompoundEditorControl(editor, args, {
        line {
            spacing = 2.0
            view(editor.name)
            operator("<-")
            view(editor.value)
        }
    })