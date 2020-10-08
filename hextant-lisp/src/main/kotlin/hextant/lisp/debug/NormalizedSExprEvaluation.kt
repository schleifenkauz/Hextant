package hextant.lisp.debug

import hextant.lisp.editor.NormalizedSExprEditor
import hextant.lisp.editor.SExprEditor
import hextant.plugin.Aspects

object NormalizedSExprEvaluation : Evaluation<NormalizedSExprEditor> {
    override fun Aspects.isNormalized(editor: NormalizedSExprEditor): Boolean = true

    override fun Aspects.transform(
        editor: NormalizedSExprEditor,
        visitor: SExprVisitor
    ): SExprEditor<*> {
        if (!visitor.enter(editor)) return editor
        doTransform(editor.wrapped, visitor)
        return visitor.exit(editor)
    }
}