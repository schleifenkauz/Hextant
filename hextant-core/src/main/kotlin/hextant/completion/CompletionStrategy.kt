package hextant.completion

import hextant.completion.CompletionResult.Match
import hextant.completion.CompletionResult.NoMatch
import java.util.*

/**
 * A completion strategy
 */
interface CompletionStrategy {
    fun match(now: String, completion: String): CompletionResult

    private object Simple : CompletionStrategy {
        override fun match(now: String, completion: String): CompletionResult {
            if (now == completion) return NoMatch
            var completionIdx = -1
            val matchedIndices = BitSet(completion.length)
            outer@ for (n in now) {
                //search associated character in completion
                inner@ while (true) {
                    ++completionIdx
                    if (completionIdx >= completion.length) return NoMatch //No associated character found in completion
                    val c = completion[completionIdx]
                    if (c == n) {
                        matchedIndices.set(completionIdx)
                        break@inner //Next character in input
                    }
                }
            }
            return Match(matchedIndices)
        }
    }

    private abstract class Words(private val includeSeparators: Boolean) : CompletionStrategy {
        protected abstract fun Char.isSeparator(): Boolean

        private fun String.words(): List<String> {
            val builder = StringBuilder()
            val res = mutableListOf<String>()
            for (c in this) {
                if (c.isSeparator()) {
                    builder.append(c)
                } else {
                    res.add(builder.toString())
                    builder.setLength(1)
                    if (includeSeparators) builder.append(c)
                }
            }
            return res
        }

        final override fun match(now: String, completion: String): CompletionResult {
            if (now == completion) return NoMatch
            val completionWords = completion.words()
            val wordsNow = completion.words()
            return if (wordsNow.size > completionWords.size) NoMatch
            else if (!wordsNow.zip(completionWords).all { (n, c) -> c.startsWith(n) }) NoMatch
            else buildMatch(wordsNow, completionWords)
        }

        private fun buildMatch(
            wordsNow: List<String>,
            completionWords: List<String>
        ): Match {
            TODO("not implemented")
        }
    }

    private object CamelCase : Words(includeSeparators = true), CompletionStrategy {
        override fun Char.isSeparator(): Boolean = isUpperCase()
    }

    private class Separators(private val separators: Set<Char>) : Words(includeSeparators = false), CompletionStrategy {
        override fun Char.isSeparator(): Boolean = this in separators
    }

    companion object {
        /**
         * A simple completion strategy the doesn't respect whitespace or capital letters
         */
        val simple: CompletionStrategy = Simple

        /**
         * The camelcase completion strategy
         */
        val camelCase: CompletionStrategy = CamelCase

        /**
         * A completion strategy that separated words by the given [separators] and then matches them
         */
        fun separators(separators: Set<Char>): CompletionStrategy = Separators(separators)

        /**
         * Vararg function for [CompletionStrategy.separators]
         */
        fun separators(vararg separators: Char): CompletionStrategy = separators(separators.toSet())

        /**
         * A separation completion strategy separating with a underscore ('_')
         */
        val underscore: CompletionStrategy = separators('_')

        /**
         * A separation completion strategy separating by a hyphen ('-')
         */
        val hyphen: CompletionStrategy = separators('-')
    }
}
