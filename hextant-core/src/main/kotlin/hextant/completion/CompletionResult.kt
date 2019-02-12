/**
 *@author Nikolaus Knop
 */

package hextant.completion

sealed class CompletionResult {
    object NoMatch : CompletionResult()

    data class Match(val matchedRegions: List<IntRange>) : CompletionResult()
}