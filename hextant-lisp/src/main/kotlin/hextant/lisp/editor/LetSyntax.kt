package hextant.lisp.editor

import hextant.context.Context
import hextant.lisp.SExpr
import hextant.lisp.Symbol
import reaktive.value.now

object LetSyntax : SpecialSyntax<LetEditor>("let", 3) {
    override fun representsEditors(editors: List<SExprEditor<*>?>): Boolean = editors[1] is SymbolEditor

    override fun represents(expressions: List<SExpr>): Boolean = expressions[1] is Symbol

    override fun representEditors(
        context: Context,
        editors: List<SExprEditor<*>?>
    ): LetEditor = LetEditor(context).apply {
        name.setText((editors[1] as SymbolEditor).text.now)
        editors[2]?.let { value.expand(it) }
        editors[3]?.let { body.expand(it) }
    }

    override fun represent(context: Context, expressions: List<SExpr>): LetEditor = LetEditor(context).apply {
        name.setText(expressions[1].toString())
        value.reconstruct(expressions[2])
        body.reconstruct(expressions[3])
    }

    override fun desugar(editor: LetEditor): SExprEditor<*> = CallExprEditor(editor.context).apply {
        expressions.addLast(SExprExpander(context, SymbolEditor(context, "let")))
        expressions.addLast(SExprExpander(context, editor.name))
        expressions.addLast(editor.value)
        expressions.addLast(editor.body)
    }

    override fun createTemplate(context: Context): LetEditor = LetEditor(context)
}