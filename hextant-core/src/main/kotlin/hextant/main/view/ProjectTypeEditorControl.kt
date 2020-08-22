/**
 *@author Nikolaus Knop
 */

package hextant.main.view

import bundles.Bundle
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorControl
import hextant.core.view.TokenEditorView

class ProjectTypeEditorControl(editor: TokenEditor<*, TokenEditorView>, args: Bundle) :
    TokenEditorControl(editor, args, ProjectTypeCompleter)