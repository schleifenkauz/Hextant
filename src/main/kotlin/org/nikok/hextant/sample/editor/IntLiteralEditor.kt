/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.editor

import org.nikok.hextant.Context
import org.nikok.hextant.core.base.DefaultRegistration
import org.nikok.hextant.core.editor.TokenEditor
import org.nikok.hextant.core.expr.view.TextEditorView
import org.nikok.hextant.sample.editable.EditableIntLiteral

class IntLiteralEditor(
    editable: EditableIntLiteral,
    context: Context
) : TokenEditor<EditableIntLiteral, TextEditorView>(editable, context) {
    init {
        registerDefault(context)
    }

    companion object : DefaultRegistration({

    })
}