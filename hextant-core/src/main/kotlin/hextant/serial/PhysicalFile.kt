/**
 *@author Nikolaus Knop
 */

package hextant.serial

import hextant.context.*
import hextant.core.Editor
import hextant.serial.SerialProperties.deserializationContext
import hextant.serial.SerialProperties.projectRoot
import reaktive.event.EventStream
import reaktive.event.event
import java.lang.ref.WeakReference
import java.nio.file.Files
import java.nio.file.Path

internal class PhysicalFile<E : Editor<*>>(
    obj: E?,
    private val path: Path,
    private val context: Context
) : VirtualFile<E> {
    private var ref = WeakReference(obj)
    private var deleted = false
    private val _read = event<E>()
    private val _write = event<E>()

    override val read: EventStream<E>
        get() = _read.stream
    override val write: EventStream<E>
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

    override fun get(): E {
        checkNotDeleted()
        return ref.get() ?: read()
    }

    @Suppress("UNCHECKED_CAST")
    private fun read(): E {
        val input = context.createInput(getRealPath())
        input.bundle[deserializationContext] = context
        val obj = input.readObject() as E
        input.close()
        _read.fire(obj)
        ref = WeakReference(obj)
        return obj
    }

    override fun inMemory(): Boolean = ref.get() != null

    override fun delete() {
        checkNotDeleted()
        val absolute = context[projectRoot].resolve(path)
        safeIO {
            Files.delete(absolute)
        }
        deleted = true
    }

    private fun getRealPath() = context[projectRoot].resolve(path)

    private fun checkNotDeleted() {
        check(!deleted) { "File already deleted" }
    }
}