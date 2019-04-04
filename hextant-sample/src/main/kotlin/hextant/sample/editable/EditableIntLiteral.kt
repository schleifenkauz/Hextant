/**
 *@author Nikolaus Knop
 */

package hextant.sample.editable

import hextant.*
import hextant.core.editable.EditableToken
import hextant.sample.ast.IntExpr
import hextant.sample.ast.IntLiteral
import reaktive.value.ReactiveValue
import reaktive.value.binding.map

class EditableIntLiteral : EditableToken<IntLiteral>(), EditableIntExpr {
    override fun compile(tok: String): CompileResult<IntLiteral> =
        tok.toIntOrNull().okOrErr { "Invalid integer literal $tok" }.map(::IntLiteral)

    override val expr: ReactiveValue<IntExpr?>
        get() = result.map { it.orNull() }
}