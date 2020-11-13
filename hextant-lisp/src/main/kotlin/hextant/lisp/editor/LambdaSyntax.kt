package hextant.lisp.editor

import hextant.context.Context
import hextant.context.withoutUndo
import hextant.lisp.SExpr
import hextant.lisp.Symbol
import hextant.lisp.rt.extractList
import hextant.lisp.rt.isList
import reaktive.value.now

object LambdaSyntax : SpecialSyntax<LambdaEditor>("lambda", 2) {
    override fun represents(expressions: List<SExpr>): Boolean =
        expressions[1].isList() && expressions[1].extractList().all { p -> p is Symbol }

    override fun representsEditors(editors: List<SExprEditor<*>?>): Boolean {
        val params = editors[1] as? CallExprEditor ?: return false
        return params.expressions.editors.now.all { it.editor.now is SymbolEditor }
    }

    override fun represent(context: Context, expressions: List<SExpr>): LambdaEditor =
        LambdaEditor(context).withoutUndo {
            for (param in expressions[1].extractList()) {
                val name = (param as Symbol).name
                parameters.addLast(SymbolEditor(context, name))
            }
            body.reconstruct(expressions[2])
        }

    override fun representEditors(
        context: Context,
        editors: List<SExprEditor<*>?>
    ): LambdaEditor = LambdaEditor(context).withoutUndo {
        val params = editors[1] as CallExprEditor
        for (parameter in params.expressions.editors.now) {
            parameters.addLast(parameter.editor.now as SymbolEditor)
        }
        editors[2]?.let { e -> body.expand(e) }
    }

    override fun desugar(editor: LambdaEditor): SExprEditor<*> = QuotationEditor(editor.context).withoutUndo {
        val e = CallExprEditor(editor.context).apply {
            for (p in editor.parameters.editors.now) {
                expressions.addLast(SExprExpander(context, p))
            }
            expressions.addLast(editor.body)
        }
        quoted.expand(e)
    }

    override fun createTemplate(context: Context): LambdaEditor = LambdaEditor(context)
}