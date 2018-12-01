/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.completion

/**
 * Used to get [Completion]s
*/
interface Completer<in T> {
    /**
     * @return the possible completions for [element] given a pool of possible completions [completionPool]
    */
    fun completions(element: String, completionPool: Collection<T>): Set<Completion>
}