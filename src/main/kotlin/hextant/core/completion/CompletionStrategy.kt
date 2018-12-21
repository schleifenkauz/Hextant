package hextant.core.completion

/**
 * A completion strategy
 */
interface CompletionStrategy {
    fun isCompletable(now: String, completion: String): Boolean

    private object Simple : CompletionStrategy {
        override fun isCompletable(now: String, completion: String): Boolean {
            if (now == completion) return false
            if (now.length > completion.length) return false
            var completionIndex = 0
            outer@ for (n in now) {
                inner@ while (true) {
                    if (completionIndex >= completion.length) return false
                    if (n == completion[completionIndex]) break@inner
                    ++completionIndex
                }
            }
            return true
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

        final override fun isCompletable(now: String, completion: String): Boolean {
            if (now == completion) return false
            val completionWords = completion.words()
            val wordsNow = completion.words()
            return if (wordsNow.size > completionWords.size) false
            else wordsNow.zip(completionWords).all { (n, c) -> c.startsWith(n) }
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
