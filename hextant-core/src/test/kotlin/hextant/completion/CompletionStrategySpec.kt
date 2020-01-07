/**
 *@author Nikolaus Knop
 */

package hextant.completion

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import hextant.completion.CompletionResult.Match
import hextant.completion.CompletionResult.NoMatch
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe

internal object CompletionStrategySpec: Spek({
    fun test(
        testData: List<Pair<Pair<String, String>, CompletionResult>>,
        s: CompletionStrategy
    ) {
        for (testCase in testData) {
            val now = testCase.first.first
            val p = testCase.first.second
            val expected = testCase.second
            action("is completable $now to $p") {
                test("should be $expected") {
                    s.match(now, p) shouldMatch equalTo(expected)
                }
            }
        }
    }

    describe("simple") {
        val s = CompletionStrategy.simple
        val testData = listOf(
            "abc" to "aaabbbccc" to Match(listOf(0..0, 3..3, 6..6)),
            "abz" to "aaabbbccc" to NoMatch,
            "h" to "hello world" to Match(listOf(0..0)),
            "z" to "hello world" to NoMatch,
            "w" to "hello world" to Match(listOf(6..6)),
            "public" to "public" to Match(listOf(0..5)),
            "publi" to "public" to Match(listOf(0..4)),
            "public void" to "public" to NoMatch
        )
        test(testData, s)
    }
    describe("hyphen") {
        val s = CompletionStrategy.underscore
        val testData = listOf(
            "abc" to "a_b_c" to NoMatch,
            "ab_d" to "abc_def" to Match(listOf(0..1, 3..4)),
            "ü" to "abc_def" to NoMatch,
            "___" to "abc_def_ghi_jkl" to Match(listOf(3..3, 7..7, 11..11)),
            "_" to "abc_def" to Match(listOf(3..3)),
            "_def_" to "abc_def_ghi" to Match(listOf(3..7)),
            "_" to "abc" to NoMatch
        )
        test(testData, s)
    }
})