/**
 *@author Nikolaus Knop
 */

package hextant.completion

/**
 * A completion of type [T]
 * @constructor
 * @property completion The item that was completed
 * @property completionText The textual representation of the completed item
 * @property inputText The user input that was completed
 * @property match A list of index ranges that represent matches between the [inputText] and the [completionText]
 * @property tooltipText The text that is displayed, when the user hovers the completion item, or `null`
 * @property infoText Additional information on the completion item, or `null` if there is no additional info
 * @property icon An image resource that points to 16x16 icon that should is display, or `null`
 */
class Completion<out T : Any>(
    val completion: T,
    val inputText: String,
    val completionText: String,
    val match: List<IntRange>,
    val tooltipText: String?,
    val infoText: String?,
    val icon: String?
) {
    /**
     * A Builder for [Completion]s
     * @constructor
     * @property completion The item that was completed
     * @property completionText The textual representation of the completed item
     * @property inputText The user input that was completed
     * @property match A list of index ranges that represent matches between the [inputText] and the [completionText]
     */
    class Builder<T : Any> internal constructor(
        val completion: T,
        val inputText: String,
        val completionText: String,
        val match: List<IntRange>
    ) {
        /**
         * The text that is displayed, when the user hovers the completion item, defaults to `null`
         */
        var tooltipText: String? = null

        /**
         * Additional information on the completion item, defaults to `null`
         */

        /**
         * Additional information on the completion item, defaults to `null`
         */
        var infoText: String? = null

        /**
         * An image resource that points to 16x16 icon that should is display, defaults to `null`
         */
        var icon: String? = null

        internal fun build(): Completion<T> =
            Completion(completion, inputText, completionText, match, tooltipText, infoText, icon)
    }
}