/**
 *@author Nikolaus Knop
 */

package hextant.config

import bundles.Bundle
import hextant.core.view.TokenEditorControl

internal class FeatureIdEditorControl (
    editor: FeatureIdEditor, args: Bundle
) : TokenEditorControl(editor, args, FeatureCompleter(FeatureType.ALL, editor.enabled))