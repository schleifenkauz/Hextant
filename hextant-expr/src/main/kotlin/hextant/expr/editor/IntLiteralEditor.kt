/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.codegen.ProvideFeature
import hextant.completion.Completion.Builder
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.expr.IntLiteral
import validated.*

@ProvideFeature
class IntLiteralEditor(
    context: Context,
    text: String
) : TokenEditor<IntLiteral, TokenEditorView>(context, text), ExprEditor<IntLiteral> {
    constructor(context: Context) : this(context, "")

    constructor(v: IntLiteral, context: Context) : this(context, v.value.toString())

    override fun compile(token: String): Validated<IntLiteral> =
        token.toIntOrNull().validated { invalid("Invalid int literal $token") }.map { IntLiteral(it) }

    object Completer : ConfiguredCompleter<Context, String>(CompletionStrategy.simple) {
        override fun completionPool(context: Context): Set<String> = setOf("666")

        override fun Builder<String>.configure(context: Context) {
            tooltipText = "666 is the number of satan"
            infoText = "Cool number"
            icon = "hextant/core/icon/warning.png"
        }
    }
}