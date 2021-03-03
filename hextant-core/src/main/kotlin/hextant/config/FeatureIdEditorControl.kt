/**
 *@author Nikolaus Knop
 */

package hextant.config

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.TokenEditorControl

internal class FeatureIdEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    editor: FeatureIdEditor, args: Bundle
) : TokenEditorControl(editor, args, FeatureCompleter(FeatureType.ALL, editor.enabled))