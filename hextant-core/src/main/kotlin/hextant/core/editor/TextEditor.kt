/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.Context
import hextant.core.editable.EditableText
import hextant.core.view.TextEditorView

class TextEditor(
    editableText: EditableText,
    context: Context
) : TokenEditor<EditableText, TextEditorView>(editableText, context)