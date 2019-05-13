/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import hextant.bundle.Bundle
import hextant.core.view.FXTokenEditorView
import hextant.lisp.editor.IntLiteralEditor

class IntLiteralEditorControl(
    editable: IntLiteralEditor,
    args: Bundle
) : FXTokenEditorView(editable, args) {
    init {
        root.styleClass.add("lisp-int-literal")
    }
}