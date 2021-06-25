/**
 *@author Nikolaus Knop
 */

package hextant.blocky.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.CompoundEditorControl

class AssignEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    editor: hextant.blocky.editor.AssignEditor, arguments: Bundle
) : CompoundEditorControl(editor, arguments, {
    line {
        spacing = 2.0
        view(editor.name)
        operator("<-")
        view(editor.value)
    }
})