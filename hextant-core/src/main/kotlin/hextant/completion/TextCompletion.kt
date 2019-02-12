/**
 *@author Nikolaus Knop
 */

package hextant.completion

import hextant.completion.CompletionResult.Match
import javafx.scene.control.Label
import javafx.scene.control.Tooltip

class TextCompletion<out T>(
    override val match: Match,
    override val completed: T,
    override val text: String,
    private val tooltipText: String
) : Completion<T> {
    override fun createNode() = Label(text).apply {
        isFocusTraversable = true
        tooltip = Tooltip(tooltipText)
        styleClass.addAll("completion", "hextant-text")
    }
}