/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import bundles.Bundle
import hextant.core.view.TokenEditorControl
import hextant.sample.editor.IntLiteralEditor

class IntLiteralEditorControl(
    editable: IntLiteralEditor,
    args: Bundle
) : TokenEditorControl(editable, args) {
    init {
        styleClass.add("int-literal-editor")
    }
}