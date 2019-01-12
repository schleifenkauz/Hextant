package hextant.lisp.editable

import hextant.core.editable.EditableToken
import hextant.lisp.Identifier
import hextant.lisp.Util

class EditableIdentifier : EditableToken<Identifier>() {
    override fun isValid(tok: String): Boolean = Util.isValidIdentifier(tok)

    override fun compile(tok: String): Identifier = tok
}