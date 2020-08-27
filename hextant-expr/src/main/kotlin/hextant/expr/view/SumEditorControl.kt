/**
 *@author Nikolaus Knop
 */

package hextant.expr.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.CompoundEditorControl
import hextant.expr.editor.SumEditor

class SumEditorControl @ProvideImplementation(ControlFactory::class) constructor(editor: SumEditor, arguments: Bundle) :
    CompoundEditorControl(editor, arguments, {
        line {
            keyword("sum")
            space()
            view(editor.expressions)
        }
    })