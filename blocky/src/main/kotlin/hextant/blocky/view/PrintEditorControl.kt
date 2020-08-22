package hextant.blocky.view

import bundles.Bundle
import hextant.blocky.editor.PrintEditor
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.CompoundEditorControl

class PrintEditorControl @ProvideImplementation(ControlFactory::class, PrintEditor::class) constructor(
    editor: PrintEditor, args: Bundle
) : CompoundEditorControl(editor, args, {
    line {
        spacing = 2.0
        keyword("print")
        view(editor.expr)
    }
})