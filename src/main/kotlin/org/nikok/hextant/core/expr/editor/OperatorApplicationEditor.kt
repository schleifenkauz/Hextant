/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editor

import org.nikok.hextant.core.base.AbstractEditor
import org.nikok.hextant.core.expr.editable.EditableOperatorApplication
import org.nikok.hextant.core.expr.edited.Expr
import org.nikok.hextant.core.expr.view.FXOperatorApplicationEditorView
import org.nikok.reaktive.value.now

class OperatorApplicationEditor(
    editable: EditableOperatorApplication
) : AbstractEditor<EditableOperatorApplication, FXOperatorApplicationEditorView>(editable), ExprEditor {
    override val expr: Expr?
        get() = editable.edited.now
}