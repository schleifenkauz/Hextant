package hextant.expr.editor

import hextant.Context
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter

object SpecialNumbers : ConfiguredCompleter<Context, Int>(CompletionStrategy.simple) {
    override fun completionPool(context: Context): Collection<Int> = listOf(666, 42)
}