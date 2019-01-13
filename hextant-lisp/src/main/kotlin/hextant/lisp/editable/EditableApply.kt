/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.base.AbstractEditable
import hextant.lisp.Apply
import org.nikok.reaktive.dependencies
import org.nikok.reaktive.value.ReactiveValue
import org.nikok.reaktive.value.binding.binding
import org.nikok.reaktive.value.now

class EditableApply : AbstractEditable<Apply>(), EditableSExpr<Apply> {
    val editableApplied = ExpandableSExpr()

    val editableArgs = EditableSExprList()

    override val edited: ReactiveValue<Apply?> =
        binding<Apply?>("edited", dependencies(editableApplied.edited, editableArgs.edited)) {
            val func = editableApplied.edited.now ?: return@binding null
            val args = editableArgs.editedList.now
            if (args.any { it == null }) return@binding null
            Apply(func, args.requireNoNulls())
        }
}