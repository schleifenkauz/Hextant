/**
 *@author Nikolaus Knop
 */

package hextant.sample.editable

import hextant.CompileResult
import hextant.core.editable.EditableToken
import hextant.okOrErr
import hextant.sample.ast.IntOperator

class EditableIntOperator : EditableToken<IntOperator>() {
    override fun compile(tok: String): CompileResult<IntOperator> =
        IntOperator.operatorMap[tok].okOrErr { "Invalid operator $tok" }
}