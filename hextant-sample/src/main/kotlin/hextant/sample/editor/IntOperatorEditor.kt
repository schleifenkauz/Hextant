/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.sample.ast.IntOperator
import validated.*

class IntOperatorEditor(context: Context) : TokenEditor<IntOperator>(context) {
    override fun compile(token: String): Validated<IntOperator> =
        IntOperator.operatorMap[token].validated { invalid("Invalid operator $token") }
}