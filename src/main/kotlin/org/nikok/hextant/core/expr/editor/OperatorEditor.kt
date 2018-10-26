/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editor

import org.nikok.hextant.EditorView
import org.nikok.hextant.core.base.AbstractEditor
import org.nikok.hextant.core.expr.editable.EditableOperator

class OperatorEditor(
    editable: EditableOperator,
    view: EditorView
) : AbstractEditor<EditableOperator>(editable, view)