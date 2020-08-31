/**
 *@author Nikolaus Knop
 */

package hextant.config

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.TokenEditorControl

internal class EnabledEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    editor: EnabledEditor, args: Bundle
) : TokenEditorControl(editor, args, EnabledCompleter(editor.enabled))