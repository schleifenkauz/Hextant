/**
 *@author Nikolaus Knop
 */

package hextant.completion

import kotlinx.coroutines.joinAll

/**
 * A [Completer] that is composed of multiple sub-completers and builds the union of all completions
 */
open class CompoundCompleter<Ctx, T : Any>(setup: CompoundCompleter<Ctx, T>.() -> Unit) : Completer<Ctx> {
    private val completers: MutableList<Completer<Ctx>> = mutableListOf()

    init {
        setup(this)
    }

    /**
     * Add a new sub-completer, that will be used to gather completions.
     */
    fun addCompleter(completer: Completer<Ctx>) {
        completers.add(completer)
    }

    override suspend fun collectCompletions(
        context: Ctx, input: String,
        collector: CompletionCollector
    ) {
        val collectors = mutableListOf<CompletionCollector>()
        for (completer in completers) {
            val subCollector = collector.subCollector()
            collectors.add(subCollector)
            with(completer) {
                collectCompletions(context, input, subCollector)
            }
        }
        collectors.joinAll()
        collector.finished()
    }
}