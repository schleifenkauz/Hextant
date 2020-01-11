/**
 *@author Nikolaus Knop
 */

package hextant.serial

import hextant.*
import hextant.serial.SerialProperties.projectRoot
import java.nio.file.Files
import java.nio.file.Path

@Suppress("UNCHECKED_CAST")
internal class HextantFileManagerImpl(private val context: Context) : HextantFileManager {
    private val cache = mutableMapOf<ReactivePath, HextantFileImpl<*>>()

    override fun <T : Any> get(content: T, path: ReactivePath): HextantFile<T> =
        cache.getOrPut(path) { HextantFileImpl(content, path, context) } as HextantFile<T>

    override fun <T : Editor<*>> get(editor: T): HextantFile<T> {
        check(editor.isRoot) { "Editor must be root of editor tree" }
        val f = editor.file ?: error("Editor is not part of a file")
        return get(editor, f.path)
    }

    override fun <T : Any> from(path: ReactivePath): HextantFile<T> {
        check(path !in cache) { "Already read $path" }
        return HextantFileImpl(null, path, context)
    }

    override fun rename(old: Path, new: Path) {
        val pr = context[projectRoot]
        Files.move(pr.resolve(old), pr.resolve(new))
    }

    override fun createDirectory(path: Path) {
        Files.createDirectory(context[projectRoot].resolve(path))
    }

    override fun delete(path: ReactivePath) {
        val f = cache.remove(path) ?: error("$path is not managed by this file manager")
        f.delete()
        val absolute = context[projectRoot].resolve(path.now)
        if (Files.isDirectory(absolute)) {
            Files.walk(absolute).sorted(Comparator.reverseOrder()).forEach { Files.delete(it) }
        } else Files.delete(absolute)
    }
}