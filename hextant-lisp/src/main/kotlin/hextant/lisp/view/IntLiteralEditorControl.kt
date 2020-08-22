/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import bundles.Bundle
import hextant.core.view.TokenEditorControl
import hextant.lisp.editor.IntLiteralEditor

class IntLiteralEditorControl constructor(
    editable: IntLiteralEditor,
    args: Bundle
) : TokenEditorControl(editable, args) {
    init {
        root.styleClass.add("lisp-int-literal")
    }
}