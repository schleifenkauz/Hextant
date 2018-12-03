/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.completion

/**
 * A completer that produced [SimpleCompletion]s
*/
open class SimpleCompleter<T : Any>(private val completionStrategy: CompletionStrategy) : Completer<T> {
    /**
     * @return the [String] representing the receiver, by default used [toString]
    */
    protected open fun T.getText(): String = toString()

    override fun completions(element: String, completionPool: Collection<T>): Set<Completion<T>> {
        return completionPool.asSequence()
            .map { it to it.getText() }
            .filter { (_, text) -> completionStrategy.isCompletable(element, text) }
            .map { (el, text) -> SimpleCompletion(el, text) }.toSet()
    }

}