package hextant.sample.view

import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.context.Context
import hextant.sample.BinaryOperator

object BinaryOperatorCompleter : ConfiguredCompleter<Context, BinaryOperator>(CompletionStrategy.simple) {
    override fun completionPool(context: Context): Collection<BinaryOperator> = BinaryOperator.values().asList()
}