package hextant.lisp.editable

import hextant.CompileResult
import hextant.core.editable.EditableToken
import hextant.lisp.Identifier
import hextant.lisp.Util
import hextant.okOrErr

class EditableIdentifier : EditableToken<Identifier>() {
    override fun compile(tok: String): CompileResult<Identifier> =
        tok.takeIf { Util.isValidIdentifier(it) }.okOrErr { "Invalid identifier $tok" }
}