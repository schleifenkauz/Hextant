/**
 *@author Nikolaus Knop
 */

package hextant.config

import bundles.Bundle
import hextant.core.view.TokenEditorControl

internal class EnabledEditorControl(editor: EnabledEditor, args: Bundle) :
    TokenEditorControl(editor, args, EnabledCompleter)