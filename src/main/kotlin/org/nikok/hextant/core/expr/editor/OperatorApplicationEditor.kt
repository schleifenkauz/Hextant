/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editor

import org.nikok.hextant.EditorView
import org.nikok.hextant.core.base.AbstractEditor
import org.nikok.hextant.core.expr.editable.EditableOperatorApplication

class OperatorApplicationEditor(
    editable: EditableOperatorApplication,
    view: EditorView
) : AbstractEditor<EditableOperatorApplication>(editable, view)