package hextant.lisp.debug

import hextant.codegen.ProvideImplementation
import hextant.lisp.editor.LambdaEditor
import hextant.lisp.editor.SExprEditor
import hextant.plugin.Aspects

@ProvideImplementation
object LambdaEvaluation : Evaluation<LambdaEditor> {
    override fun Aspects.isNormalized(editor: LambdaEditor): Boolean = true

    override fun Aspects.transform(editor: LambdaEditor, visitor: SExprVisitor): SExprEditor<*> {
        if (!visitor.enter(editor)) return editor
        doTransform(editor.body, visitor)
        return visitor.exit(editor)
    }
}