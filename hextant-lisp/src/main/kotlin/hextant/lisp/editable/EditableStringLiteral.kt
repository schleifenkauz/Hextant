/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.CompileResult
import hextant.Ok
import hextant.core.editable.EditableToken
import hextant.lisp.StringLiteral

class EditableStringLiteral() : EditableToken<StringLiteral>(), EditableSExpr<StringLiteral> {
    constructor(value: String) : this() {
        text.set(value)
    }

    constructor(value: StringLiteral) : this(value.value)

    override fun compile(tok: String): CompileResult<StringLiteral> = Ok(StringLiteral(tok))
}