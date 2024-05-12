package hextant.core.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.context.createControl
import hextant.core.editor.TransformedEditor

@ProvideImplementation(ControlFactory::class)
internal fun createControl(editor: TransformedEditor<*, *>, arguments: Bundle) =
    editor.context.createControl(editor.source, arguments)