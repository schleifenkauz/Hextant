/**
 *@author Nikolaus Knop
 */

package hextant.serial

import hextant.*
import hextant.serial.SerialProperties.projectRoot
import reaktive.event.EventStream
import reaktive.event.event
import java.lang.ref.WeakReference
import java.nio.file.Files

internal class HextantFileImpl<T : Any>(
    obj: T?,
    private val path: ReactivePath,
    private val context: Context
) : HextantFile<T> {
    private var ref = WeakReference(obj)
    private var deleted = false
    private val _read = event<T>()
    private val _write = event<T>()

    override val read: EventStream<T>
        get() = _read.stream
    override val write: EventStream<T>
        get() = _write.stream

    override fun write() {
        checkNotDeleted()
        val obj = ref.get()
        checkNotNull(obj) { "Already collected" }
        _write.fire(obj)
        safeIO {
            val output = context.createOutput(getRealPath())
            output.writeObject(obj)
            output.close()
        }
    }

    override fun get(): T {
        checkNotDeleted()
        return ref.get() ?: read()
    }

    @Suppress("UNCHECKED_CAST")
    private fun read(): T {
        val input = context.createInput(getRealPath())
        val obj = input.readObject() as T
        input.close()
        _read.fire(obj)
        ref = WeakReference(obj)
        return obj
    }

    override fun inMemory(): Boolean = ref.get() != null

    override fun delete() {
        checkNotDeleted()
        val absolute = context[projectRoot].resolve(path.now)
        safeIO {
            Files.delete(absolute)
        }
        deleted = true
    }

    private fun getRealPath() = context[projectRoot].resolve(path.now)

    private fun checkNotDeleted() {
        check(!deleted) { "File already deleted" }
    }
}