/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import hextant.Context
import hextant.bundle.Bundle
import hextant.core.view.FXTokenEditorView
import hextant.lisp.editor.IdentifierEditor

class IdentifierEditorControl(
    editable: IdentifierEditor,
    context: Context,
    args: Bundle
) : FXTokenEditorView(editable, context, args)