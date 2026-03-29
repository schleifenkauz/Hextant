package hextant.completion

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import java.util.*

class CompletionCollector private constructor(
    private val limit: Int,
    private val queue: PriorityQueue<Completion<*>> = PriorityQueue<Completion<*>>()
) {
    private val job = CompletableDeferred<Unit>()

    fun finished() {
        job.complete(Unit)
    }

    fun offerCompletion(similarity: Int, completion: () -> Completion<*>) {
        if (queue.size < limit || queue.peek()!!.similarity < similarity) {
            queue.add(completion())
            if (queue.size > limit) {
                queue.poll()
            }
        }
    }

    fun getCompletions() = queue.reversed()

    fun get(): Deferred<Unit> = job

    fun subCollector() = CompletionCollector(limit, queue)

    companion object {
        fun limit(maxItems: Int) = CompletionCollector(maxItems)
    }
}