/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.*
import hextant.base.AbstractEditable
import hextant.lisp.*
import reaktive.collection.binding.all
import reaktive.dependencies
import reaktive.value.binding.binding
import reaktive.value.now

class EditableApply : AbstractEditable<Apply>(), EditableSExpr<Apply> {
    val editableExpressions = EditableSExprList()

    private val expressions get() = editableExpressions.resultList

    private val subexpressionsOk = expressions.all { it.isOk }

    @Suppress("UNCHECKED_CAST")
    override val result: RResult<Apply> = binding<CompileResult<Apply>>(dependencies(expressions)) {
        if (subexpressionsOk.now)
            Ok(Apply(SinglyLinkedList.fromList(expressions.now as List<SExpr>)))
        else ChildErr
    }
}