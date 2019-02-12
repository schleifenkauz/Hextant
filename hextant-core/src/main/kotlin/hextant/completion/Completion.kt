/**
 *@author Nikolaus Knop
 */

package hextant.completion

import javafx.scene.control.Control

/**
 * A completion
 */
interface Completion<out T> {
    /**
     * @return the completed text
     * for "hel" it could be "hello world"
     */
    val text: String

    /**
     * The completion result
     */
    val match: CompletionResult.Match

    /**
     * The completed element
     */
    val completed: T

    /**
     * @return a [Control] showing this completion
     */
    fun createNode(): Control
}