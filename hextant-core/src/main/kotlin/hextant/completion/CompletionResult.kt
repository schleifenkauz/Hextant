/**
 *@author Nikolaus Knop
 */

package hextant.completion

import java.util.*

sealed class CompletionResult {
    object NoMatch : CompletionResult()

    data class Match(val matchedIndices: BitSet) : CompletionResult()
}