package hextant.lisp.editor

import hextant.*
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.lisp.Identifier
import hextant.lisp.Util

class IdentifierEditor(context: Context) : TokenEditor<Identifier, TokenEditorView>(context) {
    override fun compile(token: String): CompileResult<Identifier> =
        token.takeIf { Util.isValidIdentifier(it) }.okOrErr { "Invalid identifier $token" }
}