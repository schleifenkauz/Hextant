package hextant.expr.editor

import hextant.*
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.expr.Operator

class OperatorEditor(context: Context, text: String) : TokenEditor<Operator, TokenEditorView>(context, text) {
    constructor(context: Context, operator: Operator) : this(context, operator.name)
    constructor(context: Context) : this(context, "")

    override fun compile(token: String): CompileResult<Operator> =
        token.takeIf { Operator.isValid(it) }.okOrErr { "Invalid operator $token" }.map { Operator.of(it) }
}
