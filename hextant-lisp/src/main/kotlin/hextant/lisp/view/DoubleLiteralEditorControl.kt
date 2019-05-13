/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import hextant.bundle.Bundle
import hextant.core.view.FXTokenEditorView
import hextant.lisp.editor.DoubleLiteralEditor

class DoubleLiteralEditorControl(
    editable: DoubleLiteralEditor,
    args: Bundle
) : FXTokenEditorView(editable, args) {
    init {
        root.styleClass.add("lisp-double-literal")
    }
}