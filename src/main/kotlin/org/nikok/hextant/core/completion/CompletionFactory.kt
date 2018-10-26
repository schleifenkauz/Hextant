/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.completion

/**
 * Used to create a completion from a completed element
*/
interface CompletionFactory<T> {
    /**
     * @return the [Completion] for the specified [completed] element
    */
    fun getCompletion(completed: T): Completion
}