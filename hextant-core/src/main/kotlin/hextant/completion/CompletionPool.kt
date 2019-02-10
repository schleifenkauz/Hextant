/**
 * @author Nikolaus Knop
 */

package hextant.completion

interface CompletionPool<C> {
    fun pool(): Set<C>
}