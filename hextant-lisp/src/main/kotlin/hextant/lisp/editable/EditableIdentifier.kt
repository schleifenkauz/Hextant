package hextant.lisp.editable

import hextant.CompileResult
import hextant.core.editable.EditableToken
import hextant.lisp.Identifier
import hextant.lisp.Util
import hextant.okIfOrErr

class EditableIdentifier : EditableToken<Identifier>() {
    override fun compile(tok: String): CompileResult<Identifier> =
        tok.okIfOrErr(Util.isValidIdentifier(tok)) { "Invalid identifier $tok" }
}