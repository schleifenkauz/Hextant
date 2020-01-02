/**
 *@author Nikolaus Knop
 */

package hextant.serial

import kserial.*
import reaktive.event.EventStream
import reaktive.event.event
import java.lang.ref.WeakReference
import java.nio.file.Files
import java.nio.file.Path

internal class HextantFileImpl<T : Any>(
    obj: T,
    private val path: Path,
    private val serial: KSerial,
    private val context: SerialContext
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
        serial.createOutput(path, context).use { out ->
            out.writeObject(obj)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(): T {
        checkNotDeleted()
        return ref.get() ?: serial.createInput(path, context).use { input ->
            val obj = input.readObject() as T
            _read.fire(obj)
            ref = WeakReference(obj)
            obj
        }
    }

    override fun inMemory(): Boolean = ref.get() != null

    override fun delete() {
        checkNotDeleted()
        Files.delete(path)
        deleted = true
    }

    private fun checkNotDeleted() {
        check(!deleted) { "File already deleted" }
    }
}