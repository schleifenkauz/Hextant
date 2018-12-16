/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editable

import kserial.Serializable
import org.nikok.hextant.core.editable.EditableToken
import org.nikok.hextant.core.expr.edited.IntLiteral

class EditableIntLiteral() : EditableToken<IntLiteral>(), Serializable {
    constructor(v: Int) : this() {
        text.set(v.toString())
    }

    override fun isValid(tok: String): Boolean = tok.toIntOrNull() != null

    override fun compile(tok: String): IntLiteral = IntLiteral(tok.toInt())
}