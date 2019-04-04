/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.RResult
import hextant.base.AbstractEditable
import hextant.lisp.FileScope
import hextant.lisp.GetVal
import hextant.mapOrChildErr
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
        ident.mapOrChildErr { GetVal(it, fileScope) }
    }
}