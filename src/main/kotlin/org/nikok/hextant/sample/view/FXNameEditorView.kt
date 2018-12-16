/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.view

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.expr.view.FXTokenEditorView
import org.nikok.hextant.sample.editable.EditableName

class FXNameEditorView(
    editable: EditableName,
    platform: HextantPlatform
) : FXTokenEditorView(editable, platform) {
    init {
        root.styleClass.add("identifier-editor")
    }
}