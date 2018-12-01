/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.completion

/**
 * A base class for [Completer]s
 * @constructor
 * @param strategy the used [CompletionStrategy]
 * @param factory the used [CompletionFactory]
*/
open class ConfiguredCompleter<in T : Any>(
    private val strategy: CompletionStrategy, private val factory: CompletionFactory<T>
) : Completer<T> {
    /**
     * @return the text representing the receiver, by default used [toString]
    */
    protected open fun T.getText(): String = toString()

    override fun completions(element: String, completionPool: Collection<T>): Set<Completion> {
        val completions = completionPool.asSequence().filter {
            strategy.isCompletable(element, it.getText())
        }.map { factory.getCompletion(it) }
        return completions.toSet()
    }
}