/**
 *@author Nikolaus Knop
 */

package hextant.serial

import kserial.KSerial
import kserial.SerialContext
import java.nio.file.Path

@Suppress("UNCHECKED_CAST")
internal class HextantFileManagerImpl(
    private val serial: KSerial,
    private val context: SerialContext
) : HextantFileManager {
    private val cached = mutableMapOf<Path, HextantFile<*>>()

    override fun <T : Any> create(path: Path, content: T): HextantFile<T> {
        check(path !in cached) { "File $path already exists" }
        val f = HextantFileImpl(content, path, serial, context)
        cached[path] = f
        return f
    }

    override fun <T : Any> get(path: Path): HextantFile<T> = cached.getValue(path) as HextantFile<T>

    override fun <T : Any> get(path: Path, content: T): HextantFile<T> =
        cached.getOrPut(path) { HextantFileImpl(content, path, serial, context) } as HextantFile<T>

    override fun delete(path: Path) {
        cached.remove(path)?.delete() ?: error("File $path does not exist")
    }
}