package hextant.completion

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import java.util.*

class CompletionCollector private constructor(
    private val limit: Int,
    private val queue: PriorityQueue<Completion<*>>,
    private val job: CompletableJob = Job()
): Job by job {
    fun finished() {
        job.complete()
    }

    fun offerCompletion(similarity: Double, completion: () -> Completion<*>) {
        if (similarity <= 0) return
        if (queue.size < limit || queue.peek()!!.similarity < similarity) {
            queue.offer(completion())
            if (queue.size > limit) {
                queue.poll()
            }
        }
    }

    fun getCompletions() = queue.sortedByDescending(Completion<*>::similarity) //seems to be necessary for some reason...

    fun subCollector() = CompletionCollector(limit, queue)

    companion object {
        fun limit(maxItems: Int): CompletionCollector {
            val queue = PriorityQueue(compareBy(Completion<*>::similarity))
            return CompletionCollector(maxItems, queue)
        }
    }
}