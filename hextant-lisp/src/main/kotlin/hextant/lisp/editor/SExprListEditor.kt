/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.Editor
import hextant.core.list.EditableList
import hextant.core.list.ListEditor
import hextant.lisp.editable.*

class SExprListEditor(
    list: EditableSExprList,
    context: Context
) : ListEditor<EditableSExpr<*>, EditableSExprList>(list, context) {
    override fun createNewEditable(): EditableSExpr<*> = ExpandableSExpr()

    override fun accepts(child: Editor<*>): Boolean = child.editable is EditableSExpr<*>
}