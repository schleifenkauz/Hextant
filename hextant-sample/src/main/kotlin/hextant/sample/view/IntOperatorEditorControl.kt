/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import bundles.Bundle
import hextant.core.view.TokenEditorControl
import hextant.sample.editor.IntOperatorEditor

class IntOperatorEditorControl(
    editor: IntOperatorEditor,
    args: Bundle
) : TokenEditorControl(editor, args) {
    init {
        styleClass.add("int-operator-editor")
    }
}