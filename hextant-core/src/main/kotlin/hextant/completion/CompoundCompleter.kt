/**
 *@author Nikolaus Knop
 */

package hextant.completion

/**
 * A [Completer] that is composed of multiple sub-completers and builds the union of all completions
 */
class CompoundCompleter<Ctx, T> : Completer<Ctx, T> {
    private val completers: MutableList<Completer<Ctx, T>> = mutableListOf()

    /**
     * Add a new sub-completer, that will be used to gather completions.
     */
    fun addCompleter(completer: Completer<Ctx, T>) {
        completers.add(completer)
    }

    override fun completions(context: Ctx, input: String): Collection<Completion<T>> {
        val completions = mutableListOf<Completion<T>>()
        for (completer in completers) {
            completions.addAll(completer.completions(context, input))
        }
        return completions
    }
}