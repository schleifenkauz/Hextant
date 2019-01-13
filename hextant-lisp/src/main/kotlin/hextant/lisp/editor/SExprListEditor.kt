/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.Editor
import hextant.core.list.EditableList
import hextant.core.list.ListEditor
import hextant.lisp.editable.EditableSExpr
import hextant.lisp.editable.ExpandableSExpr

class SExprListEditor(
    list: EditableList<*, EditableSExpr<*>>,
    context: Context
) : ListEditor<EditableSExpr<*>>(list, context) {
    override fun createNewEditable(): EditableSExpr<*> = ExpandableSExpr()

    override fun accepts(child: Editor<*>): Boolean = child.editable is EditableSExpr<*>
}