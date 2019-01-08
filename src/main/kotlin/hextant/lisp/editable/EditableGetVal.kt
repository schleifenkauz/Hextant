/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.base.AbstractEditable
import hextant.lisp.FileScope
import hextant.lisp.GetVal
import org.nikok.reaktive.value.ReactiveValue
import org.nikok.reaktive.value.reactiveValue

class EditableGetVal(
    private val fileScope: FileScope,
    private val scope: Scope
) : AbstractEditable<GetVal>(),
    EditableSExpr<GetVal> {
    val searchedIdentifier = EditableIdentifier()

    override val edited: ReactiveValue<GetVal?> = searchedIdentifier.edited.flatMap("edited") { ident ->
        if (ident == null) reactiveValue("null", null)
        else scope.resolve(ident).map("edited") { resolved ->
            if (resolved == null) null
            else GetVal(ident, fileScope)
        }
    }
}