/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.editable

import org.nikok.hextant.core.editable.EditableToken
import org.nikok.hextant.sample.ast.IntExpr
import org.nikok.hextant.sample.ast.IntLiteral
import org.nikok.reaktive.value.ReactiveValue

class EditableIntLiteral : EditableToken<IntLiteral>(), EditableIntExpr {
    override fun isValid(tok: String): Boolean = tok.toIntOrNull() != null

    override fun compile(tok: String): IntLiteral = IntLiteral(tok.toInt())

    override val expr: ReactiveValue<IntExpr?>
        get() = edited
}