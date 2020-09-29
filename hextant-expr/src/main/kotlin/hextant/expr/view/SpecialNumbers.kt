package hextant.expr.view

import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter

object SpecialNumbers : ConfiguredCompleter<Any, Int>(CompletionStrategy.simple) {
    override fun completionPool(context: Any): Collection<Int> = listOf(666, 42)
}