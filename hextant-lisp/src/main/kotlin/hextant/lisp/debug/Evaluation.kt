/**
 * @author Nikolaus Knop
 */

package hextant.lisp.debug

import hextant.codegen.RequestAspect
import hextant.lisp.editor.SExprEditor
import hextant.plugin.Aspects

@RequestAspect(optional = true)
interface Evaluation<E : SExprEditor<*>> {
    fun Aspects.evaluateFully(editor: E): SExprEditor<*> = editor

    fun Aspects.evaluateOneStep(editor: E): SExprEditor<*> = editor

    fun Aspects.canEvaluateOneStep(editor: E): Boolean = false

    fun Aspects.canEvaluateFully(editor: E): Boolean = true

    fun Aspects.isNormalized(editor: E): Boolean = false

    fun Aspects.transform(editor: E, visitor: SExprVisitor): SExprEditor<*>
}