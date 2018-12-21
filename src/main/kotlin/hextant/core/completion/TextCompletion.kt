/**
 *@author Nikolaus Knop
 */

package hextant.core.completion

import javafx.scene.control.Label
import javafx.scene.control.Tooltip

class TextCompletion<out T>(
    override val completed: T, override val text: String, private val tooltipText: String
) : Completion<T> {
    override fun createNode() = Label(text).apply {
        isFocusTraversable = true
        tooltip = Tooltip(tooltipText)
        styleClass.addAll("completion", "hextant-text")
    }
}