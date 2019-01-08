/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.base.AbstractEditable
import hextant.lisp.LambdaExpr
import org.nikok.reaktive.dependencies
import org.nikok.reaktive.value.ReactiveValue
import org.nikok.reaktive.value.binding.binding
import org.nikok.reaktive.value.now

class EditableLambda : AbstractEditable<LambdaExpr>(), EditableSExpr<LambdaExpr> {
    val editableParameters = EditableIdentifierList()

    val editableBody = ExpandableSExpr()

    override val edited: ReactiveValue<LambdaExpr?> =
        binding<LambdaExpr?>("edited", dependencies(editableParameters.editedList, editableBody.edited)) {
            val parameters = editableParameters.editedList.now
            val body = editableBody.edited.now ?: return@binding null
            if (parameters.any { it == null }) return@binding null
            LambdaExpr(parameters.requireNoNulls(), body)
        }
}