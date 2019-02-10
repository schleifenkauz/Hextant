/**
 *@author Nikolaus Knop
 */

package hextant.completion

/**
 * A base class for [Completer]s
 * @constructor
 * @param strategy the used [CompletionStrategy]
 * @param factory the used [CompletionFactory]
 */
open class ConfiguredCompleter<C : Any>(
    private val strategy: CompletionStrategy,
    private val factory: CompletionFactory<C>,
    private val pool: CompletionPool<C>
) : Completer<C> {
    /**
     * @return the text representing the receiver, by default used [toString]
     */
    protected open fun C.getText(): String = toString()

    override fun completions(input: String): Set<Completion<C>> {
        val completions = pool.pool().asSequence().filter {
            strategy.isCompletable(input, it.getText())
        }.map { factory.getCompletion(it) }
        return completions.toSet()
    }
}