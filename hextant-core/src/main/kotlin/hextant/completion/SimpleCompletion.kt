/**
 * @author Nikolaus Knop
 */

package hextant.completion

import hextant.completion.CompletionResult.Match
import javafx.scene.control.Control
import javafx.scene.control.Label

/**
 * A simple completion
 * @constructor
 * @param text the completed text
 * @param match the matching result
 *
 */
class SimpleCompletion<out T>(
    override val completed: T,
    override val text: String,
    override val match: Match
) : Completion<T> {
    override fun createNode(): Control = Label(text).apply {
        isFocusTraversable = true
        styleClass.addAll("completion", "hextant-text")
    }
}