/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import bundles.Bundle
import hextant.core.view.TokenEditorControl
import hextant.lisp.editor.DoubleLiteralEditor

class DoubleLiteralEditorControl(
    editable: DoubleLiteralEditor,
    args: Bundle
) : TokenEditorControl(editable, args) {
    init {
        root.styleClass.add("lisp-double-literal")
    }
}