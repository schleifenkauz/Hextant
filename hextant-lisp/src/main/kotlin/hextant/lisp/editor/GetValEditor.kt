/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.lisp.GetVal
import reaktive.value.binding.map
import validated.Validated.InvalidComponent
import validated.map
import validated.or
import validated.reaktive.ReactiveValidated

class GetValEditor(context: Context) : CompoundEditor<GetVal>(context), SExprEditor<GetVal> {
    constructor(value: String, context: Context) : this(context) {
        searchedIdentifier.setText(value)
    }

    private val fileScope = context[LispProperties.fileScope]

    val searchedIdentifier by child(IdentifierEditor(context))

    override val result: ReactiveValidated<GetVal> = searchedIdentifier.result.map { ident ->
        ident.or(InvalidComponent).map { GetVal(it, fileScope) }
    }
}