/**
 *@author Nikolaus Knop
 */

package hextant.completion

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe

internal object CompletionStrategySpec: Spek({
    data class CompletionTestCase(val now: String, val possibility: String, val shouldComplete: Boolean)

    fun Pair<Pair<String, String>, Boolean>.testCase() = CompletionTestCase(
        first.first, first.second,
        second
    )
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
        ).map { it.testCase() }
        for ((now, p, expected) in testData) {
            action("is completable $now to $p") {
                test("should be $expected") {
                    s.isCompletable(now, p) shouldMatch equalTo(expected)
                }
            }
        }
    }
})