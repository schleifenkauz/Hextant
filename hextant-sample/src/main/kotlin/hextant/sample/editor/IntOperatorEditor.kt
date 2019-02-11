/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.sample.editable.EditableIntOperator

class IntOperatorEditor(
    editable: EditableIntOperator,
    context: Context
) : TokenEditor<EditableIntOperator, TokenEditorView>(editable, context)