package hextant.blocky.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.CompoundEditorControl

class PrintEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    private val editor: hextant.blocky.editor.PrintEditor, arguments: Bundle
) : CompoundEditorControl(editor, arguments) {
    override fun build(): Layout = horizontal {
        root.spacing = 2.0
        keyword("print")
        view(editor.expr)
    }

}