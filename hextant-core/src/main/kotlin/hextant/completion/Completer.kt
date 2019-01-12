/**
 * @author Nikolaus Knop
 */

package hextant.completion

/**
 * Used to get [Completion]s
 */
interface Completer<T> {
    /**
     * @return the possible completions for [element] given a pool of possible completions [completionPool]
     */
    fun completions(element: String, completionPool: Collection<T>): Set<Completion<T>>
}