/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.*
import hextant.core.editable.EditableToken
import hextant.lisp.IntLiteral

class EditableIntLiteral() : EditableToken<IntLiteral>(), EditableSExpr<IntLiteral> {
    constructor(value: Int) : this() {
        text.set(value.toString())
    }

    constructor(value: IntLiteral) : this(value.value)

    override fun compile(tok: String): CompileResult<IntLiteral> =
        tok.toIntOrNull().okOrErr { "Invalid int literal $tok" }.map(::IntLiteral)
}