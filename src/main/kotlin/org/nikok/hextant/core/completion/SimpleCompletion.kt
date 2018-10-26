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
class SimpleCompletion(override val completed: String): Completion {
    override fun createNode(): Control = Label(completed).apply { styleClass.add("completion") }
}