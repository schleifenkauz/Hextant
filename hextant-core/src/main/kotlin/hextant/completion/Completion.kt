/**
 *@author Nikolaus Knop
 */

package hextant.completion

import org.kordamp.ikonli.Ikon

/**
 * A completion of type [T]
 * @constructor
 * @property item The item that was completed
 * @property completionText The textual representation of the completed item
 * @property inputText The user input that was completed
 * @property match A list of index ranges that represent matches between the [inputText] and the [completionText]
 * @property tooltipText The text that is displayed, when the user hovers the completion item
 * @property infoText Additional information on the completion item, or `null` if there is no additional info
 * @property icon An [Ikon] that should be displayed next to the completion info, or `null` if there is no icon.
 */
class Completion<out T : Any>(
    val item: T,
    val inputText: String,
    val completionText: String,
    val match: List<IntRange>,
    val similarity: Double,
    val tooltipText: String?,
    val infoText: String?,
    val icon: Ikon?,
    val source: Completer<*>
) {
    override fun toString(): String = "Completion [$inputText -> $completionText], similarity: $similarity"

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
        val match: List<IntRange>,
        val similarity: Double,
        private val source: Completer<*>
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
         * An [Ikon] that should be displayed next to the completion info. `null by default.
         */
        var icon: Ikon? = null

        internal fun build(): Completion<T> =
            Completion(
                completion, inputText, completionText, match, similarity,
                tooltipText, infoText, icon, source
            )
    }
}