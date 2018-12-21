/**
 *@author Nikolaus Knop
 */

package hextant.sample.editable

import hextant.core.editable.EditableToken
import hextant.sample.ast.IntOperator

class EditableIntOperator : EditableToken<IntOperator>() {
    override fun isValid(tok: String): Boolean = tok in IntOperator.operatorMap

    override fun compile(tok: String): IntOperator = IntOperator.operatorMap[tok]!!
}