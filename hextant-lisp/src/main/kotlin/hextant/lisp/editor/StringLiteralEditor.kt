/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.lisp.StringLiteral
import validated.Validated
import validated.Validated.Valid

class StringLiteralEditor(context: Context) : TokenEditor<StringLiteral>(context), SExprEditor<StringLiteral> {
    constructor(value: String, context: Context) : this(context) {
        setText(value)
    }

    constructor(value: StringLiteral, context: Context) : this(value.value, context)

    override fun compile(token: String): Validated<StringLiteral> =
        Valid(StringLiteral(token))
}