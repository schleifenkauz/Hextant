/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.base.AbstractEditable
import hextant.lisp.FileScope
import hextant.lisp.GetVal
import org.nikok.reaktive.value.ReactiveValue

class EditableGetVal(
    private val fileScope: FileScope
) : AbstractEditable<GetVal>(),
    EditableSExpr<GetVal> {
    constructor(fileScope: FileScope, value: String) : this(fileScope) {
        searchedIdentifier.text.set(value)
    }

    val searchedIdentifier = EditableIdentifier()

    override val edited: ReactiveValue<GetVal?> = searchedIdentifier.edited.map("edited") { ident ->
        if (ident == null) null
        else GetVal(ident, fileScope)
    }
}