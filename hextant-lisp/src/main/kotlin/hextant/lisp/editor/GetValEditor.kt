/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.*
import hextant.base.CompoundEditor
import hextant.lisp.GetVal
import reaktive.value.binding.map

class GetValEditor(context: Context) : CompoundEditor<GetVal>(context), SExprEditor<GetVal> {
    constructor(value: String, context: Context) : this(context) {
        searchedIdentifier.setText(value)
    }

    private val fileScope = context[LispProperties.fileScope]

    val searchedIdentifier by child(IdentifierEditor(context))

    override val result: EditorResult<GetVal> = searchedIdentifier.result.map { ident ->
        ident.or(ChildErr).map { GetVal(it, fileScope) }
    }
}