/**
 *@author Nikolaus Knop
 */

package hextant.serial

import hextant.context.Context
import hextant.core.Editor
import hextant.serial.SerialProperties.projectRoot
import reaktive.event.EventStream
import reaktive.event.event
import java.io.File
import java.lang.ref.WeakReference

/**
 * An implementation of a [VirtualFile] that is associated with a concrete physical file.
 */
class PhysicalFile<E : Editor<*>>(
    obj: E?,
    private val path: File,
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
            obj.saveSnapshotAsJson(getRealPath())
        }
    }

    override fun get(): E {
        checkNotDeleted()
        return ref.get() ?: read()
    }

    @Suppress("UNCHECKED_CAST")
    private fun read(): E {
        val editor = reconstructEditorFromJSONSnapshot(path, context) as E
        _read.fire(editor)
        ref = WeakReference(editor)
        return editor
    }

    override fun inMemory(): Boolean = ref.get() != null

    override fun delete() {
        checkNotDeleted()
        val absolute = context[projectRoot].resolve(path)
        safeIO {
            absolute.delete()
        }
        deleted = true
    }

    private fun getRealPath() = context[projectRoot].resolve(path)

    private fun checkNotDeleted() {
        check(!deleted) { "File already deleted" }
    }
}