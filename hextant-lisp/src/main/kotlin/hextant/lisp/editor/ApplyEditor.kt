/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.base.CompoundEditor
import hextant.lisp.*
import reaktive.collection.binding.all
import reaktive.dependencies
import reaktive.value.binding.binding
import reaktive.value.now
import validated.Validated
import validated.Validated.InvalidComponent
import validated.Validated.Valid
import validated.isValid
import validated.reaktive.ReactiveValidated

class ApplyEditor(context: Context) : CompoundEditor<Apply>(context), SExprEditor<Apply> {
    val editableExpressions by child(SExprListEditor(context))

    private val expressions get() = editableExpressions.results

    private val subexpressionsOk = expressions.all { it.isValid }

    @Suppress("UNCHECKED_CAST")
    override val result: ReactiveValidated<Apply> = binding<Validated<Apply>>(dependencies(expressions)) {
        if (subexpressionsOk.now)
            Valid(Apply(SinglyLinkedList.fromList(expressions.now as List<SExpr>)))
        else InvalidComponent
    }
}