/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.completion

import javafx.scene.control.Label
import javafx.scene.control.Tooltip

class TextCompletion(
    override val completed: String, private val text: String, private val tooltipText: String
) : Completion {
    override fun createNode() = Label(text).apply {
        isFocusTraversable = true
        tooltip = Tooltip(tooltipText)
        styleClass.addAll("completion", "hextant-text")
    }
}