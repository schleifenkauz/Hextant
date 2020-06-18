/**
 *@author Nikolaus Knop
 */

package hextant.serial

import hextant.context.Context
import hextant.core.Editor
import hextant.serial.SerialProperties.projectRoot
import org.nikok.kref.Ref
import org.nikok.kref.weak
import java.nio.file.Files
import java.nio.file.Path

internal class PhysicalFileManager(private val context: Context) : FileManager {
    private val cache = mutableMapOf<Path, Ref<PhysicalFile<*>>>()

    @Suppress("UNCHECKED_CAST")
    override fun <E : Editor<*>> get(content: E, path: Path): VirtualFile<E> {
        val cached = cache[path]?.referent
        return if (cached == null) {
            val f = PhysicalFile(content, path, content.context)
            cache[path] = weak(f)
            f
        } else cached as VirtualFile<E>
    }

    override fun <E : Editor<*>> from(path: Path, context: Context): VirtualFile<E> {
        check(path !in cache) { "Already read $path" }
        return PhysicalFile(null, path, context)
    }

    override fun rename(old: Path, new: Path) {
        val pr = context[projectRoot]
        safeIO {
            Files.move(pr.resolve(old), pr.resolve(new))
        }
    }

    override fun createDirectory(path: Path) {
        safeIO {
            Files.createDirectory(context[projectRoot].resolve(path))
        }
    }

    override fun deleteFile(path: Path) {
        safeIO {
            val f = cache.remove(path)?.referent ?: error("$path is not managed by this file manager")
            f.delete()
        }
    }

    override fun deleteDirectory(path: Path) {
        val absolute = context[projectRoot].resolve(path)
        Files.walk(absolute).sorted(Comparator.reverseOrder()).forEach { Files.delete(it) }
    }
}