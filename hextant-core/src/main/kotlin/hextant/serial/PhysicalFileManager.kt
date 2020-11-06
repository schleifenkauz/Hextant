/**
 *@author Nikolaus Knop
 */

package hextant.serial

import hextant.context.Context
import hextant.core.Editor
import hextant.serial.SerialProperties.projectRoot
import org.nikok.kref.Ref
import org.nikok.kref.weak
import java.io.File

internal class PhysicalFileManager(private val context: Context) : FileManager {
    private val cache = mutableMapOf<File, Ref<PhysicalFile<*>>>()

    @Suppress("UNCHECKED_CAST")
    override fun <E : Editor<*>> get(content: E, path: File): VirtualFile<E> {
        val cached = cache[path]?.referent
        return if (cached == null) {
            val f = PhysicalFile(content, path, content.context)
            cache[path] = weak(f)
            f
        } else cached as VirtualFile<E>
    }

    override fun <E : Editor<*>> from(path: File, context: Context): VirtualFile<E> {
        check(path !in cache) { "Already read $path" }
        return PhysicalFile(null, path, context)
    }

    override fun rename(old: File, new: File) {
        val pr = context[projectRoot]
        safeIO {
            pr.resolve(old).renameTo(pr.resolve(new))
        }
    }

    override fun createDirectory(path: File) {
        safeIO {
            context[projectRoot].resolve(path).mkdir()
        }
    }

    override fun deleteFile(path: File) {
        safeIO {
            val f = cache.remove(path)?.referent ?: error("$path is not managed by this file manager")
            f.delete()
        }
    }

    override fun deleteDirectory(dir: File) {
        val absolute = context[projectRoot].resolve(dir)
        absolute.deleteRecursively()
    }
}