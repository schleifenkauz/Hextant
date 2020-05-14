/**
 *@author Nikolaus Knop
 */

package hextant.serial

import hextant.Editor
import reaktive.event.EventStream
import reaktive.event.never

/**
 * A [VirtualFile] that always holds its contents in working memory.
 */
class InMemoryFile<E : Editor<*>>(private val editor: E) : VirtualFile<E> {
    override fun write() {}

    override fun get(): E = editor

    override fun delete() {
        throw UnsupportedOperationException("Cannot delete in memory file")
    }

    override val read: EventStream<E> = never()
    override val write: EventStream<E> = never()

    override fun inMemory(): Boolean = true
}