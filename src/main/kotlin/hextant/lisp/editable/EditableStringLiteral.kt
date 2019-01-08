/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.core.editable.EditableToken
import hextant.lisp.StringLiteral

class EditableStringLiteral : EditableToken<StringLiteral>() {
    override fun isValid(tok: String): Boolean = true

    override fun compile(tok: String): StringLiteral = StringLiteral(tok)
}