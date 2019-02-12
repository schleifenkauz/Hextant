/**
 * @author Nikolaus Knop
 */

package hextant.completion

/**
 * Used to get [Completion]s
 */
interface Completer<out C> {
    /**
     * @return the possible completions for [input]
     */
    fun completions(input: String): Set<Completion<C>>
}