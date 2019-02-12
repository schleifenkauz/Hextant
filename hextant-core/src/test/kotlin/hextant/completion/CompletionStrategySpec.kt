/**
 *@author Nikolaus Knop
 */

package hextant.completion

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import hextant.completion.CompletionResult.Match
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe

internal object CompletionStrategySpec: Spek({
    fun test(
        testData: List<Pair<Pair<String, String>, Boolean>>,
        s: CompletionStrategy
    ) {
        for (testCase in testData) {
            val now = testCase.first.first
            val p = testCase.first.second
            val expected = testCase.second
            action("is completable $now to $p") {
                test("should be $expected") {
                    (s.match(now, p) is Match) shouldMatch equalTo(expected)
                }
            }
        }
    }

    describe("simple") {
        val s = CompletionStrategy.simple
        val testData = listOf(
            "abc" to "aaabbbccc" to true,
            "abz" to "aaabbbccc" to false,
            "h" to "hello world" to true,
            "z" to "hello world" to false,
            "w" to "hello world" to true,
            "public" to "public" to false,
            "publi" to "public" to true,
            "public void" to "public" to false
        )
        test(testData, s)
    }
    describe("hyphen") {
        val s = CompletionStrategy.underscore
        val testData = listOf(
            "abc" to "a_b_c" to false,
            "ab_d" to "abc_def" to true,
            "ü" to "abc_def" to false,
            "___" to "abc_def_ghi_jkl" to true,
            "_" to "abc_def" to true,
            "_def_" to "abc_def_ghi" to true,
            "_" to "abc" to false
        )
        test(testData, s)
    }
})