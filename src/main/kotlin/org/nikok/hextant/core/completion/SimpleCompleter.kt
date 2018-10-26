/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.completion

/**
 * A completer that produced [SimpleCompletion]s
*/
abstract class SimpleCompleter<T: Any>(private val completionStrategy: CompletionStrategy) : Completer<T> {
    /**
     * @return the [String] representing the receiver, by default used [toString]
    */
    protected open fun T.getText(): String = toString()

    override fun completions(element: String, completionPool: Set<T>): Set<Completion> {
        return completionPool.asSequence()
                .map { it.getText() }
                .filter { completionStrategy.isCompletable(element, it) }
                .map(::SimpleCompletion).toSet()
    }
}