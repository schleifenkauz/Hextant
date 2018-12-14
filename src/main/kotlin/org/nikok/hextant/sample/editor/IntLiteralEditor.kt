/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.editor

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.base.DefaultRegistration
import org.nikok.hextant.core.editor.TokenEditor
import org.nikok.hextant.core.expr.view.TextEditorView
import org.nikok.hextant.sample.editable.EditableIntLiteral

class IntLiteralEditor(
    editable: EditableIntLiteral,
    platform: HextantPlatform
) : TokenEditor<EditableIntLiteral, TextEditorView>(editable, platform) {
    init {
        registerDefault(platform)
    }

    companion object : DefaultRegistration({

    })
}