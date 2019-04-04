/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.*
import hextant.base.AbstractEditable
import hextant.lisp.FileScope
import hextant.lisp.GetVal
import reaktive.value.binding.map

class EditableGetVal(
    private val fileScope: FileScope
) : AbstractEditable<GetVal>(),
    EditableSExpr<GetVal> {
    constructor(fileScope: FileScope, value: String) : this(fileScope) {
        searchedIdentifier.text.set(value)
    }

    val searchedIdentifier = EditableIdentifier()

    override val result: RResult<GetVal> = searchedIdentifier.result.map { ident ->
        ident.or(ChildErr).map { GetVal(it, fileScope) }
    }
}