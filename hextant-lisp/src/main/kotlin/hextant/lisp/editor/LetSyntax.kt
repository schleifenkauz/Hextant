package hextant.lisp.editor

import hextant.context.Context
import hextant.context.withoutUndo
import hextant.lisp.SExpr
import hextant.lisp.Symbol
import reaktive.value.now

object LetSyntax : SpecialSyntax<LetEditor>("let", 3) {
    override fun representsEditors(editors: List<SExprEditor?>): Boolean = editors[1] is SymbolEditor

    override fun represents(expressions: List<SExpr>): Boolean = expressions[1] is Symbol

    override fun representEditors(
        context: Context,
        scope: RuntimeScopeEditor,
        editors: List<SExprEditor?>
    ): LetEditor = LetEditor(context, scope).withoutUndo {
        name.setText((editors[1] as SymbolEditor).text.now)
        value.setEditor(editors[2])
        body.setEditor(editors[3])
    }

    override fun represent(context: Context, scope: RuntimeScopeEditor, expressions: List<SExpr>): LetEditor =
        LetEditor(context, scope).withoutUndo {
            name.setText(expressions[1].toString())
            value.reconstruct(expressions[2])
            body.reconstruct(expressions[3])
        }

    override fun desugar(editor: LetEditor): SExprEditor = CallExprEditor(editor.context, editor.scope).withoutUndo {
        expressions.addLast(SExprExpander(context, scope, SymbolEditor(context, scope, "let")))
        expressions.addLast(SExprExpander(context, scope, editor.name))
        expressions.addLast(editor.value)
        expressions.addLast(editor.body)
    }

    override fun createTemplate(context: Context, scope: RuntimeScopeEditor): LetEditor = LetEditor(context, scope)
}