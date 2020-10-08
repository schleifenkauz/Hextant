package hextant.lisp.debug

import hextant.codegen.ProvideImplementation
import hextant.generated.isNormalized
import hextant.lisp.editor.LetEditor
import hextant.lisp.editor.SExprEditor
import hextant.plugin.Aspects

@ProvideImplementation
object LetEvaluation : Evaluation<LetEditor> {
    override fun Aspects.evaluateFully(editor: LetEditor): SExprEditor<*> {
        TODO("not implemented")
    }

    override fun Aspects.evaluateOneStep(editor: LetEditor): SExprEditor<*> = TODO()

    private fun Aspects.canEval(editor: LetEditor) = isNormalized(editor.value)

    override fun Aspects.canEvaluateOneStep(editor: LetEditor): Boolean = canEval(editor)

    override fun Aspects.canEvaluateFully(editor: LetEditor): Boolean = canEval(editor)

    override fun Aspects.transform(editor: LetEditor, visitor: SExprVisitor): SExprEditor<*> {
        if (!visitor.enter(editor)) return editor
        doTransform(editor.value, visitor)
        doTransform(editor.body, visitor)
        return visitor.exit(editor)
    }
}