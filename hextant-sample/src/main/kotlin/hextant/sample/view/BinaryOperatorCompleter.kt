package hextant.sample.view

import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.sample.BinaryOperator

object BinaryOperatorCompleter : ConfiguredCompleter<Any, BinaryOperator>(CompletionStrategy.simple) {
    override fun completionPool(context: Any): Collection<BinaryOperator> = BinaryOperator.values().asList()
}