/**
 *@author Nikolaus Knop
 */

package hextant.sample.editable

import hextant.core.editable.EditableToken
import hextant.sample.ast.IntExpr
import hextant.sample.ast.IntLiteral
import reaktive.value.ReactiveValue

class EditableIntLiteral : EditableToken<IntLiteral>(), EditableIntExpr {
    override fun isValid(tok: String): Boolean = tok.toIntOrNull() != null

    override fun compile(tok: String): IntLiteral = IntLiteral(tok.toInt())

    override val expr: ReactiveValue<IntExpr?>
        get() = edited
}