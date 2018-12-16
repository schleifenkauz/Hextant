/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.view

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.editable.EditableToken
import org.nikok.hextant.core.expr.view.FXTokenEditorView

class FXIntLiteralEditorView(
    editable: EditableToken<Any>,
    platform: HextantPlatform
) : FXTokenEditorView(editable, platform) {
    init {
        root.styleClass.add("int-literal-editor")
    }
}