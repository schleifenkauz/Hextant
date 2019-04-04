/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.*
import hextant.core.editable.EditableToken
import hextant.lisp.DoubleLiteral

class EditableDoubleLiteral() : EditableToken<DoubleLiteral>(), EditableSExpr<DoubleLiteral> {
    constructor(value: Double) : this() {
        text.set(value.toString())
    }

    constructor(value: DoubleLiteral) : this(value.value)

    override fun compile(tok: String): CompileResult<DoubleLiteral> =
        tok.toDoubleOrNull().okOrErr { "Invalid double literal $tok" }.map(::DoubleLiteral)
}