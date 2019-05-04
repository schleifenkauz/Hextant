/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.lisp.*
import reaktive.collection.binding.all
import reaktive.dependencies
import reaktive.value.binding.binding
import reaktive.value.now

class ApplyEditor(context: Context) : AbstractEditor<Apply, EditorView>(context), SExprEditor<Apply> {
    val editableExpressions = SExprListEditor(context)

    private val expressions get() = editableExpressions.results

    private val subexpressionsOk = expressions.all { it.isOk }

    @Suppress("UNCHECKED_CAST")
    override val result: EditorResult<Apply> = binding<CompileResult<Apply>>(dependencies(expressions)) {
        if (subexpressionsOk.now)
            Ok(Apply(SinglyLinkedList.fromList(expressions.now as List<SExpr>)))
        else ChildErr
    }
}