/**
 *@author Nikolaus Knop
 */

package hextant.serial

import kserial.KSerial
import kserial.SerialContext
import java.nio.file.Path
import java.util.*

@Suppress("UNCHECKED_CAST")
internal class HextantFileManagerImpl(
    private val serial: KSerial,
    private val context: SerialContext
) : HextantFileManager {
    private val byPath = mutableMapOf<Path, HextantFileImpl<*>>()
    private val byRoot = WeakHashMap<Any, HextantFileImpl<*>>()

    override fun <T : Any> create(path: Path, content: T): HextantFile<T> {
        check(path !in byPath) { "File $path already exists" }
        val f = HextantFileImpl(content, path, serial, context)
        byPath[path] = f
        byRoot[content] = f
        return f
    }

    override fun <T : Any> get(path: Path): HextantFile<T> = byPath.getValue(path) as HextantFile<T>

    override fun <T : Any> get(content: T): HextantFile<T> = byRoot.getValue(content) as HextantFile<T>

    override fun <T : Any> get(path: Path, content: T): HextantFile<T> = if (path in byPath) {
        val f = byPath.getValue(path)
        check(f.get() === content)
        f as HextantFile<T>
    } else {
        val f = HextantFileImpl(content, path, serial, context)
        byPath[path] = f
        byRoot[content] = f
        f
    }

    override fun delete(path: Path) {
        byPath.remove(path)?.delete() ?: error("File $path does not exist")
    }
}