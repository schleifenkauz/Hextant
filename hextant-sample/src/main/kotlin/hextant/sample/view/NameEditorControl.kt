/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import bundles.Bundle
import hextant.core.view.TokenEditorControl
import hextant.sample.editor.NameEditor

class NameEditorControl(editor: NameEditor, args: Bundle) : TokenEditorControl(editor, args) {
    init {
        styleClass.add("identifier-editor")
    }
}