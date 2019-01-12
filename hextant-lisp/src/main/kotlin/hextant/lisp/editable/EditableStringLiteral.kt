/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.core.editable.EditableToken
import hextant.lisp.StringLiteral

class EditableStringLiteral() : EditableToken<StringLiteral>(), EditableSExpr<StringLiteral> {
    constructor(value: String) : this() {
        text.set(value)
    }

    override fun isValid(tok: String): Boolean = true

    override fun compile(tok: String): StringLiteral = StringLiteral(tok)
}