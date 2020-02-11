/**
 *@author Nikolaus Knop
 */

package hextant.completion

/**
 * A completion
 */
class Completion<out T>(
    val completion: T,
    val inputText: String,
    val completionText: String,
    val match: List<IntRange>,
    val tooltipText: String?,
    val infoText: String?,
    val icon: String?
) {
    class Builder<T> internal constructor(
        val completion: T,
        val inputText: String,
        val completionText: String,
        val match: List<IntRange>
    ) {
        var tooltipText: String? = null
        var infoText: String? = null
        var icon: String? = null

        fun build(): Completion<T> =
            Completion(completion, inputText, completionText, match, tooltipText, infoText, icon)
    }
}