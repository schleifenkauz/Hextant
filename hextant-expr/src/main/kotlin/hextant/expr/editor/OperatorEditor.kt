package hextant.expr.editor

import hextant.*
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.expr.edited.Operator

class OperatorEditor(context: Context) : TokenEditor<Operator, TokenEditorView>(context) {
    constructor(operator: Operator, context: Context) : this(context) {
        setText(operator.name)
    }

    override fun compile(token: String): CompileResult<Operator> =
        token.takeIf { Operator.isValid(it) }.okOrErr { "Invalid operator $token" }.map { Operator.of(it) }
}
