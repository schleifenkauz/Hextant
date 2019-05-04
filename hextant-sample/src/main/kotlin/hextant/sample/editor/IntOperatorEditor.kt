/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.*
import hextant.core.editor.TokenEditor
import hextant.sample.ast.IntOperator

class IntOperatorEditor(context: Context) : TokenEditor<IntOperator>(context) {
    override fun compile(token: String): CompileResult<IntOperator> =
        IntOperator.operatorMap[token].okOrErr { "Invalid operator $token" }
}