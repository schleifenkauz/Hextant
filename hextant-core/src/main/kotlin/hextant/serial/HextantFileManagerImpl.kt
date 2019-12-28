/**
 *@author Nikolaus Knop
 */

package hextant.serial

import kserial.KSerial
import kserial.SerialContext
import java.nio.file.Path

internal class HextantFileManagerImpl(private val serial: KSerial, private val context: SerialContext) :
    HextantFileManager {
    private val cached = mutableMapOf<Path, HextantFile<*>>()

    override fun <T : Any> create(path: Path, content: T): HextantFile<T> {
        check(path !in cached) { "File $path already exists" }
        val f = HextantFileImpl(content, path, serial, context)
        cached[path] = f
        return f
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(path: Path): HextantFile<T> = cached.getValue(path) as HextantFile<T>

    override fun delete(path: Path) {
        cached.remove(path)?.delete() ?: error("File $path does not exist")
    }
}