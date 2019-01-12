/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.core.editable.EditableToken
import hextant.lisp.DoubleLiteral

class EditableDoubleLiteral() : EditableToken<DoubleLiteral>(), EditableSExpr<DoubleLiteral> {
    constructor(value: Double) : this() {
        text.set(value.toString())
    }

    override fun isValid(tok: String): Boolean = tok.toDoubleOrNull() != null

    override fun compile(tok: String): DoubleLiteral = DoubleLiteral(tok.toDouble())
}