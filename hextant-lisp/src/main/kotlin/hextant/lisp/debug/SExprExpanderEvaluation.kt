package hextant.lisp.debug

import hextant.codegen.ProvideImplementation
import hextant.generated.*
import hextant.lisp.editor.SExprEditor
import hextant.lisp.editor.SExprExpander
import hextant.plugin.Aspects
import reaktive.value.now

@ProvideImplementation
object SExprExpanderEvaluation : Evaluation<SExprExpander> {
    override fun Aspects.evaluateFully(editor: SExprExpander): SExprEditor<*> {
        editor.setEditor(evaluateFully(editor.editor.now!!))
        return editor
    }

    override fun Aspects.evaluateOneStep(editor: SExprExpander): SExprEditor<*> {
        editor.setEditor(evaluateOneStep(editor.editor.now!!))
        return editor
    }

    override fun Aspects.canEvaluateOneStep(editor: SExprExpander): Boolean =
        editor.isExpanded && canEvaluateOneStep(editor.editor.now!!)

    override fun Aspects.canEvaluateFully(editor: SExprExpander): Boolean =
        editor.isExpanded && canEvaluateFully(editor.editor.now!!)

    override fun Aspects.isNormalized(editor: SExprExpander): Boolean =
        editor.isExpanded && isNormalized(editor.editor.now!!)

    override fun Aspects.transform(editor: SExprExpander, visitor: SExprVisitor): SExprEditor<*> {
        if (!editor.isExpanded) return editor
        editor.setEditor(doTransform(editor.editor.now!!, visitor))
        return editor
    }
}