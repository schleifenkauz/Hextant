package hextant.lisp.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.lisp.Identifier
import hextant.lisp.Util
import validated.*

class IdentifierEditor(context: Context) : TokenEditor<Identifier>(context) {
    override fun compile(token: String): Validated<Identifier> =
        token.takeIf { Util.isValidIdentifier(it) }.validated { invalid("Invalid identifier $token") }
}