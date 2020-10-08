package hextant.lisp.debug

import hextant.codegen.ProvideImplementation
import hextant.lisp.editor.*
import hextant.plugin.Aspects
import reaktive.value.now

@ProvideImplementation
object QuotationEvaluation : Evaluation<QuotationEditor> {
    override fun Aspects.evaluateFully(editor: QuotationEditor): SExprEditor<*> =
        NormalizedSExprEditor(editor.context, editor.quoted.editor.now!!)

    override fun Aspects.evaluateOneStep(editor: QuotationEditor): SExprEditor<*> = evaluateFully(editor)

    override fun Aspects.canEvaluateOneStep(editor: QuotationEditor): Boolean = editor.quoted.isExpanded

    override fun Aspects.canEvaluateFully(editor: QuotationEditor): Boolean = canEvaluateOneStep(editor)

    override fun Aspects.transform(
        editor: QuotationEditor,
        visitor: SExprVisitor
    ): SExprEditor<*> {
        if (!visitor.enter(editor)) return editor
        doTransform(editor.quoted, visitor)
        return visitor.exit(editor)
    }
}