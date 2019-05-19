/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.*
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.sample.ast.IntOperator

class IntOperatorEditor(context: Context) : TokenEditor<IntOperator, TokenEditorView>(context) {
    override fun compile(token: String): CompileResult<IntOperator> =
        IntOperator.operatorMap[token].okOrErr { "Invalid operator $token" }
}