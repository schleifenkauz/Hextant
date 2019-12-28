/**
 *@author Nikolaus Knop
 */

package hextant.serial

import kserial.*
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

    override fun write() {
        checkNotDeleted()
        val obj = ref.get()
        checkNotNull(obj) { "Already collected" }
        serial.createOutput(path, context).use { out ->
            out.writeObject(obj)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(): T {
        checkNotDeleted()
        return ref.get() ?: serial.createInput(path, context).use { input ->
            val obj = input.readObject() as T
            ref = WeakReference(obj)
            obj
        }
    }

    override fun delete() {
        checkNotDeleted()
        Files.delete(path)
        deleted = true
    }

    private fun checkNotDeleted() {
        check(!deleted) { "File already deleted" }
    }
}