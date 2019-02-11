/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.Context
import hextant.core.editable.EditableText
import hextant.core.view.TokenEditorView

class TextEditor(
    editableText: EditableText,
    context: Context
) : TokenEditor<EditableText, TokenEditorView>(editableText, context)