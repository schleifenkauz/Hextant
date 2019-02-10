/**
 *@author Nikolaus Knop
 */

package hextant.completion

/**
 * A completer that produced [SimpleCompletion]s
 */
open class SimpleCompleter<C : Any>(
    private val completionStrategy: CompletionStrategy,
    private val completions: CompletionPool<C>
) : Completer<C> {
    /**
     * @return the [String] representing the receiver, by default used [toString]
     */
    protected open fun C.getText(): String = toString()

    override fun completions(input: String): Set<Completion<C>> {
        return completions.pool().asSequence()
            .map { it to it.getText() }
            .filter { (_, text) -> completionStrategy.isCompletable(input, text) }
            .map { (el, text) -> SimpleCompletion(el, text) }.toSet()
    }

}