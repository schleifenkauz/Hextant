/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.*
import hextant.completion.AbstractCompleter
import hextant.completion.Completion.Builder
import hextant.completion.CompletionStrategy
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.expr.edited.IntLiteral

class IntLiteralEditor(
    context: Context,
    text: String
) : TokenEditor<IntLiteral, TokenEditorView>(context, text), ExprEditor<IntLiteral> {
    constructor(context: Context) : this(context, "")

    constructor(v: IntLiteral, context: Context) : this(context, v.value.toString())

    override fun compile(token: String): CompileResult<IntLiteral> =
        token.toIntOrNull().okOrErr { "Invalid int literal $token" }.map { IntLiteral(it) }

    object Completer : AbstractCompleter<Context, String>(CompletionStrategy.simple) {
        override fun completionPool(context: Context): Set<String> = setOf("666")

        override fun extractText(context: Context, item: String): String? = item

        override fun Builder<String>.configure(context: Context) {
            tooltipText = "666 is the number of satan"
            infoText = "Cool number"
            icon = "hextant/core/icon/warning.png"
        }
    }
}