/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.completion

import javafx.scene.control.Control
import javafx.scene.control.Label

/**
 * A simple completion
 * @constructor
 * @param completed the completed text
 */
class SimpleCompletion<T>(
    override val completed: T,
    override val text: String
) : Completion<T> {
    override fun createNode(): Control = Label(text).apply { styleClass.add("completion") }
}