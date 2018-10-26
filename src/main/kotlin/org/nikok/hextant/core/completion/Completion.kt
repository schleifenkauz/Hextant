/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.completion

import javafx.scene.control.Control

/**
 * A completion
*/
interface Completion {
    /**
     * @return the completed text
     * for "hel" it could be "helloworld"
    */
    val completed: String

    /**
     * @return a [Control] showing this completion
    */
    fun createNode(): Control
}