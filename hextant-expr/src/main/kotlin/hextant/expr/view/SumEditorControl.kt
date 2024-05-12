/**
 *@author Nikolaus Knop
 */

package hextant.expr.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.CompoundEditorControl
import hextant.expr.editor.SumEditor

class SumEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    private val editor: SumEditor, arguments: Bundle
) : CompoundEditorControl(editor, arguments) {
    override fun build(): Layout = horizontal {
        operator("∑")
        view(editor.expressions)
    }

}