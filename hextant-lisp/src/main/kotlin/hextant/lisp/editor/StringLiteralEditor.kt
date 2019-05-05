/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.*
import hextant.core.editor.TokenEditor
import hextant.lisp.StringLiteral

class StringLiteralEditor(context: Context) : TokenEditor<StringLiteral>(context), SExprEditor<StringLiteral> {
    constructor(value: String, context: Context) : this(context) {
        setText(value)
    }

    constructor(value: StringLiteral, context: Context) : this(value.value, context)

    override fun compile(token: String): CompileResult<StringLiteral> = Ok(StringLiteral(token))
}