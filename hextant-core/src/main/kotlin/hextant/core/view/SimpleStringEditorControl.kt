/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.editor.SimpleStringEditor

internal class SimpleStringEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    editor: SimpleStringEditor,
    arguments: Bundle
) : TokenEditorControl(editor, arguments)