/**
 *@author Nikolaus Knop
 */

package hextant.completion

/**
 * The result of matching input text with a completion
 */
sealed class CompletionResult {
    /**
     * Indicates that the strings could not be matched.
     */
    object NoMatch : CompletionResult()

    /**
     * Indicates that the string could be matched.
     * @constructor
     * @property matchedRegions the index ranges of the matched regions
     */
    data class Match(val matchedRegions: List<IntRange>) : CompletionResult()
}