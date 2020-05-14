/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import bundles.Bundle
import hextant.core.view.FXTokenEditorView
import hextant.lisp.editor.IdentifierEditor

class IdentifierEditorControl(
    editable: IdentifierEditor,
    args: Bundle
) : FXTokenEditorView(editable, args)