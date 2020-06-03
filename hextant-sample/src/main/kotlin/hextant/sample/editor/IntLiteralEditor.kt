/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.sample.ast.IntExpr
import hextant.sample.ast.IntLiteral
import reaktive.value.ReactiveValue
import reaktive.value.binding.map
import validated.*

class IntLiteralEditor(context: Context) : TokenEditor<IntLiteral, TokenEditorView>(context), IntExprEditor {
    override fun compile(token: String): Validated<IntLiteral> =
        token.toIntOrNull().validated { invalid("Invalid integer literal $token") }.map(::IntLiteral)

    override val expr: ReactiveValue<IntExpr?>
        get() = result.map { it.orNull() }
}