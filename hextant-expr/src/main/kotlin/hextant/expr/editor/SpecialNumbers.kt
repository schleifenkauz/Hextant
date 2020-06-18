package hextant.expr.editor

import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.context.Context

object SpecialNumbers : ConfiguredCompleter<Context, Int>(CompletionStrategy.simple) {
    override fun completionPool(context: Context): Collection<Int> = listOf(666, 42)
}