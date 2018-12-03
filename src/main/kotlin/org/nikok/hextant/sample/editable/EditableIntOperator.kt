/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.editable

import org.nikok.hextant.core.editable.EditableToken
import org.nikok.hextant.sample.ast.IntOperator

class EditableIntOperator : EditableToken<IntOperator>() {
    override fun isValid(tok: String): Boolean = tok in IntOperator.operatorMap

    override fun compile(tok: String): IntOperator = IntOperator.operatorMap[tok]!!
}