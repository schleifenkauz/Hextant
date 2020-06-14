/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import bundles.Bundle
import hextant.core.view.TokenEditorControl
import hextant.lisp.editor.IdentifierEditor

class IdentifierEditorControl(
    editable: IdentifierEditor,
    args: Bundle
) : TokenEditorControl(editable, args)