package hextant.lisp.debug

import hextant.codegen.ProvideImplementation
import hextant.lisp.editor.*
import hextant.plugin.Aspects
import reaktive.value.now

@ProvideImplementation
object QuasiQuotationEvaluation : Evaluation<QuasiQuotationEditor> {
    override fun Aspects.evaluateFully(editor: QuasiQuotationEditor): SExprEditor<*> =
        NormalizedSExprEditor(editor.context, editor.quoted.editor.now!!)

    override fun Aspects.canEvaluateOneStep(editor: QuasiQuotationEditor): Boolean = false

    override fun Aspects.canEvaluateFully(editor: QuasiQuotationEditor): Boolean = editor.quoted.isExpanded

    override fun Aspects.transform(
        editor: QuasiQuotationEditor,
        visitor: SExprVisitor
    ): SExprEditor<*> {
        if (!visitor.enter(editor)) return editor
        doTransform(editor.quoted, visitor)
        return visitor.exit(editor)
    }
}