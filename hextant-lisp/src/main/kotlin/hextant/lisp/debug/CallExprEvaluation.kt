package hextant.lisp.debug

import hextant.codegen.ProvideImplementation
import hextant.lisp.editor.CallExprEditor
import hextant.lisp.editor.SExprEditor
import hextant.plugin.Aspects

@ProvideImplementation
object CallExprEvaluation : Evaluation<CallExprEditor> {
    override fun Aspects.evaluateFully(editor: CallExprEditor): SExprEditor<*> {
        TODO("not implemented")
    }

    override fun Aspects.evaluateOneStep(editor: CallExprEditor): SExprEditor<*> {
        TODO("not implemented")
    }

    override fun Aspects.canEvaluateOneStep(editor: CallExprEditor): Boolean {
        TODO("not implemented")
    }

    override fun Aspects.canEvaluateFully(editor: CallExprEditor): Boolean {
        TODO("not implemented")
    }

    override fun Aspects.isNormalized(editor: CallExprEditor): Boolean = false

    override fun Aspects.transform(
        editor: CallExprEditor,
        visitor: SExprVisitor
    ): SExprEditor<*> {
        if (!visitor.enter(editor)) return editor
        for (expr in editor.expressions.editors.now) doTransform(expr, visitor)
        return visitor.exit(editor)
    }
}

